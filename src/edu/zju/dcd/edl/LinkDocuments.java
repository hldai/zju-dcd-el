package edu.zju.dcd.edl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;

import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.cg.IndexedAliasDictWithPse;
import edu.zju.dcd.edl.config.ArgParser;
import edu.zju.dcd.edl.feature.FeatureLoader;
import edu.zju.dcd.edl.feature.TfIdfExtractor;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.linker.SimpleNaiveLinker;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.tac.LinkingBasisDoc;
import edu.zju.dcd.edl.tac.LinkingBasisGen;
import edu.zju.dcd.edl.tac.MidFilter;
import edu.zju.dcd.edl.tac.QueryReader;
import edu.zju.dcd.edl.utils.WidMidMapper;

public class LinkDocuments {
	private static final String DEF_ALIAS_FILE_NAME = "dict_fb_wiki_alias_pse.txt"; 
	private static final String DEF_ALIAS_INDEX_FILE_NAME = "dict_fb_wiki_alias_index_pse.txt";
	private static final String DEF_MID_FILE_NAME = "dict_fb_wiki_mid_pse.d";
	private static final String DEF_PER_LIST_FILE_NAME = "person_list.txt";
	
	private static final String DEF_FEAT_FILE_NAME = "enwiki_tfidf.sparf";
	private static final String DEF_FEAT_INDEX_FILE_NAME = "enwiki_tfidf_index.midx";
	private static final String DEF_IDF_FILE_NAME = "enwiki_idf.sd";

	private static final String DEF_MID_WID_LIST_FILE_NAME = "mid_to_wid_full_ord_mid.txt";
	
//	private static final String DEF_FILTER_MID_LIST_FILE_NAME = "filter_mids_10_8.bin";
	
	private static final String NIL = "NIL";
	
	private static CandidatesRetriever initCandidateRetriever(String dataDir) {
		String aliasFilePath = Paths.get(dataDir, DEF_ALIAS_FILE_NAME).toString();
		String aliasIndexFilePath = Paths.get(dataDir, DEF_ALIAS_INDEX_FILE_NAME).toString();
		String midPath = Paths.get(dataDir, DEF_MID_FILE_NAME).toString();
		
		String personListFilePath = Paths.get(dataDir, DEF_PER_LIST_FILE_NAME).toString();
		
		IndexedAliasDictWithPse indexedAliasDictWithPse = new IndexedAliasDictWithPse(
				aliasFilePath, aliasIndexFilePath, midPath);
		return new CandidatesRetriever(indexedAliasDictWithPse, null, personListFilePath, null);
	}
	
	private static FeatureLoader initFeatureLoader(String dataDir) {
		String featureFileName = Paths.get(dataDir, DEF_FEAT_FILE_NAME).toString(),
				featureIdxFileName = Paths.get(dataDir, DEF_FEAT_INDEX_FILE_NAME).toString();
		return new FeatureLoader(featureFileName, featureIdxFileName);
	}
	
	private static TfIdfExtractor initTfIdfExtractor(String dataDir) {
		String idfFileName = Paths.get(dataDir, DEF_IDF_FILE_NAME).toString();
		return new TfIdfExtractor(idfFileName);
	}
	
	private static WidMidMapper initMidWidMapper(String dataDir) {
		String midWidFileName = Paths.get(dataDir, DEF_MID_WID_LIST_FILE_NAME).toString();
		return new WidMidMapper(midWidFileName);
	}
	
	private static SimpleNaiveLinker initLinker(String dataDir) {		
//		String filterMidsFileName = Paths.get(dataDir, DEF_FILTER_MID_LIST_FILE_NAME).toString();
		MidFilter midFilter = null;
//		if (filterMidsFileName != null)
//			midFilter = new MidFilter(filterMidsFileName);

		return new SimpleNaiveLinker(null, midFilter, null);
	}
	
	private static void link(String dataDir, String docDir, String mentionFileName, String resultFileName) {
		CandidatesRetriever candidatesRetriever = initCandidateRetriever(dataDir);
		FeatureLoader featureLoader = initFeatureLoader(dataDir);
		TfIdfExtractor tfIdfExtractor = initTfIdfExtractor(dataDir);
		WidMidMapper midWidMapper = initMidWidMapper(dataDir);
		
		SimpleNaiveLinker linker = initLinker(dataDir);
		
		LinkingBasisGen linkingBasisGen = new LinkingBasisGen(candidatesRetriever, featureLoader, tfIdfExtractor,
					null, midWidMapper);
		Document[] documents = QueryReader.toDocuments(mentionFileName);
		BufferedWriter writer = IOUtils.getUTF8BufWriter(resultFileName, false);
		int mentionCnt = 0, docCnt = 0;
		try {
			for (Document doc : documents) {
				++docCnt;
				mentionCnt += doc.mentions.length;
				System.out.println("processing " + docCnt + " " + doc.docId + " " + doc.mentions.length);
				
				doc.loadText(docDir);
				LinkingBasisDoc linkingBasisDoc = linkingBasisGen.getLinkingBasisDoc(doc, 40);  // TODO
				doc.text = null;
				
				LinkingResult[] results = null;
				results = linker.link(linkingBasisDoc);
				
				for (LinkingResult result : results) {
					String kbid = NIL;
					if (!result.kbid.startsWith(NIL)) {
						int wid = midWidMapper.getWid(result.kbid);
						if (wid > -1)
							kbid = String.valueOf(wid);
					}
					
					writer.write(result.queryId + "\t" + kbid + "\n");
					
//					if (result.kbid.startsWith(NIL))
//						result.kbid = NIL;
//					writer.write(result.queryId + "\t" + result.kbid + "\n");
//					resultList.add(result);
				}
//				linkingBasisDoc.toFile(dos);
				
				if (docCnt == 3)
					break;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(mentionCnt + " mentions. " + docCnt + " documents.");
	}
	
	public static void main(String args[]) {
		ArgParser argParser = new ArgParser();
		if (!argParser.parse(args))
			System.out.println("Illegal arguments.");
		String dataDir = argParser.getValue("-datadir");
		String docDir = argParser.getValue("-docdir");
		String mentionFileName = argParser.getValue("-mentions");
		String resultFileName = argParser.getValue("-result");
		
//		System.out.println(dataDir);
//		System.out.println(docDir);
//		System.out.println(mentionFileName);
//		System.out.println(resultFileName);
		link(dataDir, docDir, mentionFileName, resultFileName);
	}
}
