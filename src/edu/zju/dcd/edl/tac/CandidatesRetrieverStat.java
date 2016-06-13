// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.tac;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.config.ConfigUtils;
import edu.zju.dcd.edl.config.IniFile;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.LinkingResult;

public class CandidatesRetrieverStat {
	public static void genStat(IniFile config) {
//		AliasDict dict = ConfigUtils.getAliasDict(config.getSection("dict"));
//		CandidatesRetriever candidatesRetriever = new CandidatesRetriever(dict);
		
//		IndexedAliasDictWithPse indexedAliasDictWithPse = ConfigUtils.getAliasDictWithPse(config.getSection("dict"));
//		CandidatesRetriever candidatesRetriever = new CandidatesRetriever(indexedAliasDictWithPse);

		CandidatesRetriever candidatesRetriever = ConfigUtils.getCandidateRetriever(config.getSection("dict"), null);
		
		MidToEidMapper mteMapper = ConfigUtils.getMidToEidMapper(config
				.getSection("tac2014"));
		IniFile.Section sect = config.getSection("candidate_retrieve_stat");
		String queryFileName = sect.getValue("query_file"), goldFileName = sect
				.getValue("gold_file"), errorFileName = sect.getValue("error_file");
		genStat(candidatesRetriever, mteMapper, queryFileName, goldFileName, errorFileName);
	}

	public static void genStat(CandidatesRetriever candidatesRetriever, MidToEidMapper mapper,
			String queryFileName, String goldFileName, String errorFileName) {
		LinkingResult.ComparatorOnQueryId cmpOnQueryId = new LinkingResult.ComparatorOnQueryId();
		LinkingResult[] goldResults = LinkingResult.getGroudTruth(goldFileName);
		Arrays.sort(goldResults, cmpOnQueryId);
		BufferedWriter writer = IOUtils.getUTF8BufWriter(errorFileName, false);
		Document[] documents = QueryReader.toDocuments(queryFileName);
		int queryCnt = 0, hitCnt = 0, inKbCnt = 0, inKbHitCnt = 0;
		int candidateCnt = 0;
		LinkingResult tmpResult = new LinkingResult();
		for (Document doc : documents) {
			System.out.println(doc.docId);
			queryCnt += doc.mentions.length;
						
			CandidatesRetriever.CandidatesOfMention[] candidatesOfMentions = candidatesRetriever
					.getCandidatesInDocument(doc);
			for (int i = 0; i < candidatesOfMentions.length; ++i) {
				tmpResult.queryId = doc.mentions[i].queryId;
				if (candidatesOfMentions[i].candidates != null) {
					candidateCnt += candidatesOfMentions[i].candidates.length;
				}
				
				int pos = Arrays.binarySearch(goldResults, tmpResult,
						cmpOnQueryId);
				if (pos < 0) {
					System.err
							.println("error: query id not found in ground truth. "
									+ tmpResult.queryId);
					break;
				}

				boolean hitFlg = false;
				String goldKbId = goldResults[pos].kbid;
				if (goldKbId.startsWith(ELConsts.NIL)) {
					++hitCnt;
					hitFlg = true;
				} else {
					++inKbCnt;

					if (candidatesOfMentions[i].candidates != null) {
						int candCnt = 0;
						for (CandidatesRetriever.CandidateWithPopularity candidate : candidatesOfMentions[i].candidates) {
							String eid = mapper.getEid(candidate.mid);
							if (eid != null && eid.equals(goldKbId)) {
								++hitCnt;
								++inKbHitCnt;
								hitFlg = true;
								break;
							}
							if (++candCnt == 100)
								break;
						}
					}
				}
				
				if (!hitFlg) {
					try {
						writer.write(doc.docId + "\t" + doc.mentions[i].nameString + "\t" + goldResults[pos].queryId + "\t" + goldResults[pos].kbid + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(queryCnt + " queries. " + inKbCnt
				+ " queries in KB.");
		System.out.println("Average number of candidates: " + (double)candidateCnt / queryCnt);
		System.out.println("Recall: " + (double) hitCnt / queryCnt);
		System.out.println("In KB recall: " + (double) inKbHitCnt / inKbCnt);
	}
}
