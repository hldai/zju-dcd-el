// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import dcd.config.ConfigUtils;
import dcd.config.IniFile;
import dcd.el.ELConsts;
import dcd.el.dict.AliasDict;
import dcd.el.dict.CandidatesRetriever;
import dcd.el.feature.FeatureLoader;
import dcd.el.feature.FeaturePack;
import dcd.el.feature.TfIdfExtractor;
import dcd.el.feature.TfIdfFeature;
import dcd.el.io.IOUtils;
import dcd.el.objects.ByteArrayString;
import dcd.el.objects.Document;
import dcd.el.objects.Mention;

public class CandidateFeatureGen {
	public static void run(IniFile config) {
		AliasDict dict = ConfigUtils.getAliasDict(config.getSection("dict"));
		IniFile.Section featSect = config.getSection("feature");
		FeatureLoader featureLoader = ConfigUtils.getFeatureLoader(featSect);
//		return ;
		TfIdfExtractor tfidfExtractor = ConfigUtils.getTfIdfExtractor(featSect);

		String job = config.getValue("main", "job");
		IniFile.Section sect = config.getSection(job);
		
		if (sect == null)
			return;
		String queryFileName = sect.getValue("query_file"), srcDocPath = sect
				.getValue("src_doc_path"), dstFileName = sect
				.getValue("dst_file");

		genCandidateFeature(dict, featureLoader, tfidfExtractor, queryFileName,
				srcDocPath, dstFileName);
	}

	public static void genCandidateFeature(AliasDict dict,
			FeatureLoader featureLoader, TfIdfExtractor tfIdfExtractor,
			String queryFile, String srcDocPath, String dstFileName) {
		CandidatesRetriever candidatesRetriever = new CandidatesRetriever(dict);
		Document[] documents = QueryReader.toDocuments(queryFile);
		DataOutputStream dos = IOUtils.getBufferedDataOutputStream(dstFileName);
		int mentionCnt = 0, docCnt = 0;
		try {
			for (Document doc : documents) {				
				++docCnt;
				System.out.println("processing " + docCnt + " " + doc.docId);
				
				CandidatesRetriever.Candidates[] candidates = candidatesRetriever.getCandidatesInDocument(doc);
				
				IOUtils.writeStringVaryLen(dos, doc.docId);
				dos.writeInt(doc.mentions.length);

				doc.loadText(srcDocPath);
				TfIdfFeature tfIdfFeature = tfIdfExtractor.getTfIdf(doc.text);
				doc.text = null; // in case of memory shortage
				for (int i = 0; i < doc.mentions.length; ++i) {
					Mention mention = doc.mentions[i];
					
					IOUtils.writeStringAsByteArr(dos, mention.queryId,
							ELConsts.QUERY_ID_BYTE_LEN);
					++mentionCnt;

					LinkedList<ByteArrayString> mids = candidates[i].mids;
					if (mids == null) {
						dos.writeInt(0);
					} else {
						dos.writeInt(mids.size());
//						System.out.println(mids.size());

						FeaturePack[] featPacks = featureLoader
								.loadFeaturePacks(mids);
						int ix = 0;
						for (ByteArrayString mid : mids) {
							mid.toFileWithFixedLen(dos, ELConsts.MID_BYTE_LEN);

							if (featPacks[ix] == null) {
								dos.writeFloat(0);
								dos.writeDouble(0);
//								 System.out.println(ix + "\tnull");
							} else {
								dos.writeFloat(featPacks[ix].popularity.value);

								if (tfIdfFeature == null)
									System.out.println("tfIdfFeat");
								if (featPacks[ix].tfidf == null)
									System.out.println("tfidf");
								double sim = TfIdfFeature.similarity(
										tfIdfFeature, featPacks[ix].tfidf);
								dos.writeDouble(sim);
								// System.out.println(i + "\t" +
								// featPacks[i].popularity.value + "\t" + sim);
							}
							++ix;
						}
					}
				}
				
//				if (docCnt == 1) break;
			}

			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(mentionCnt + " mentions. " + docCnt + " documents.");
	}
}
