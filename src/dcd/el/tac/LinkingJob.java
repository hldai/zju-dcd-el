// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import dcd.config.ConfigUtils;
import dcd.config.IniFile;
import dcd.el.dict.AliasDict;
import dcd.el.feature.FeatureLoader;
import dcd.el.feature.TfIdfExtractor;
import dcd.el.io.IOUtils;
import dcd.el.linker.LinkerWithAliasDict;
import dcd.el.linker.NaiveLinker;
import dcd.el.linker.RandomLinker;
import dcd.el.linker.SimpleNaiveLinker;
import dcd.el.objects.Document;
import dcd.el.objects.LinkingResult;

public class LinkingJob {
	public static void run(IniFile config) {
		IniFile.Section mainSect = config.getSection("main");
		String job = mainSect.getValue("job"), linkerName = mainSect
				.getValue("linker");
		System.out.println(linkerName);

		if (job.startsWith("link_with_cand_feat")) {
			if (linkerName.equals("naive")) {
				SimpleNaiveLinker simpleNaiveLinker = getSimpleNaiveLinker(config);
				processCandidateLocalFeatureFile(config, job, simpleNaiveLinker);
			}
		} else if (job.equals("link_full")) {
			LinkerWithAliasDict linker = getFullLinker(config, linkerName);

			IniFile.Section querySect = config.getSection("query");
			if (querySect == null) {
				System.out.println("Couldn't find the query section.");
				return;
			}

			processQueryFile(querySect, linker);
			// TODO close
		}
	}

	private static void processCandidateLocalFeatureFile(IniFile config, String job,
			SimpleNaiveLinker simpleNaiveLinker) {
		IniFile.Section sect = config.getSection(job);
		String candFeatFileName = sect.getValue("cand_feat_file"), goldFileName = sect
				.getValue("gold_file"), resultFileName = sect
				.getValue("result_file"), errorListFileName = sect.getValue("error_list_file");

		DataInputStream dis = IOUtils
				.getBufferedDataInputStream(candFeatFileName);
		DocMentionCandidates dmc = new DocMentionCandidates();
		LinkedList<LinkingResult> resultList = new LinkedList<LinkingResult>();
		while (dmc.fromFile(dis)) {
			LinkingResult[] results = simpleNaiveLinker.link14(dmc);
			for (LinkingResult result : results) {
				// writer.write(result.queryId + "\t" + result.kbid + "\n");
				resultList.add(result);
			}
		}

		Collections.sort(resultList, new LinkingResult.ComparatorOnQueryId());
		writeLinkingResultsToFile(resultList, resultFileName);

		Scorer.score(goldFileName, resultFileName, errorListFileName);
	}

	private static void processQueryFile(IniFile.Section querySect,
			LinkerWithAliasDict linker) {
		String queryFileName = querySect.getValue("query_file"), srcDocPath = querySect
				.getValue("src_doc_path"), resultFileName = querySect
				.getValue("result_file"), goldFileName = querySect
				.getValue("gold_file");

		processQueryFile(linker, queryFileName, srcDocPath, resultFileName);
		Scorer.score(goldFileName, resultFileName, null);

		// System.out.println("Time used for retrieving candidates: "
		// + dict.getRetrieveTime() / 1000.0);
		// System.out.println("Time used for retrieving features: "
		// + npl.getRetrievePopularityTime() / 1000.0);
	}

	public static void processQueryFile(LinkerWithAliasDict linker,
			String queryFileName, String srcDocPath, String resultFileName) {
		Document[] documents = QueryReader.toDocuments(queryFileName);
		LinkedList<LinkingResult> allResults = new LinkedList<LinkingResult>();
		for (Document doc : documents) {
			doc.loadText(srcDocPath);
			LinkingResult[] results = linker.link(doc);
			for (LinkingResult result : results) {
				allResults.add(result);
			}
			doc.text = null;
		}
		
		Collections.sort(allResults, new LinkingResult.ComparatorOnQueryId());
		writeLinkingResultsToFile(allResults, resultFileName);
	}

	private static LinkerWithAliasDict getFullLinker(IniFile config,
			String linkerName) {
		IniFile.Section tac2014Sect = config.getSection("tac2014"), dictSect = config
				.getSection("dict");

		AliasDict dict = ConfigUtils.getAliasDict(dictSect);
		MidToEidMapper mteMapper = ConfigUtils.getMidToEidMapper(tac2014Sect);

		if (linkerName.equals("random")) {
			return new RandomLinker(dict, mteMapper);
		} else if (linkerName.equals("naive")) {
			IniFile.Section featSect = config.getSection("feature");
			TfIdfExtractor tfIdfExtractor = ConfigUtils
					.getTfIdfExtractor(featSect);
			FeatureLoader featureLoader = ConfigUtils
					.getFeatureLoader(featSect);

			return new NaiveLinker(dict, tfIdfExtractor, featureLoader,
					mteMapper);
		}
		return null;
	}

	private static SimpleNaiveLinker getSimpleNaiveLinker(IniFile config) {
		IniFile.Section tac2014Sect = config.getSection("tac2014");
		MidToEidMapper mteMapper = ConfigUtils.getMidToEidMapper(tac2014Sect);
		
//		System.out.println("eid: " + mteMapper.getEid("010005"));
		return new SimpleNaiveLinker(mteMapper);
	}
	
	private static void writeLinkingResultsToFile(LinkedList<LinkingResult> results, String resultFileName) {
		BufferedWriter writer = IOUtils.getUTF8BufWriter(resultFileName, false);
		try {
			for (LinkingResult result : results) {
				writer.write(result.queryId + "\t" + result.kbid + "\t" + result.confidence + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
