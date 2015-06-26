// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import dcd.config.ConfigUtils;
import dcd.config.IniFile;
import dcd.el.ELConsts;
import dcd.el.dict.AliasDict;
import dcd.el.dict.CandidatesRetriever;
import dcd.el.io.IOUtils;
import dcd.el.objects.ByteArrayString;
import dcd.el.objects.Document;
import dcd.el.objects.LinkingResult;
import dcd.el.utils.CommonUtils;

public class CandidatesRetrieverStat {
	public static void genStat(IniFile config) {
		AliasDict dict = ConfigUtils.getAliasDict(config.getSection("dict"));
		MidToEidMapper mteMapper = ConfigUtils.getMidToEidMapper(config
				.getSection("tac2014"));
		IniFile.Section sect = config.getSection("candidate_retrieve_stat");
		String queryFileName = sect.getValue("query_file"), goldFileName = sect
				.getValue("gold_file"), errorFileName = sect.getValue("error_file");
		genStat(dict, mteMapper, queryFileName, goldFileName, errorFileName);
	}

	public static void genStat(AliasDict dict, MidToEidMapper mapper,
			String queryFileName, String goldFileName, String errorFileName) {
		LinkingResult.ComparatorOnQueryId cmpOnQueryId = new LinkingResult.ComparatorOnQueryId();
		LinkingResult[] goldResults = getGroudTruth(goldFileName);
		Arrays.sort(goldResults, cmpOnQueryId);
		BufferedWriter writer = IOUtils.getUTF8BufWriter(errorFileName, false);
		CandidatesRetriever candidatesRetriever = new CandidatesRetriever(dict);
		Document[] documents = QueryReader.toDocuments(queryFileName);
		int queryCnt = 0, hitCnt = 0, inKbCnt = 0, inKbHitCnt = 0;
		int candidateCnt = 0;
		LinkingResult tmpResult = new LinkingResult();
		for (Document doc : documents) {
			System.out.println(doc.docId);
			queryCnt += doc.mentions.length;
			CandidatesRetriever.Candidates[] candidates = candidatesRetriever
					.getCandidatesInDocument(doc);
			for (int i = 0; i < candidates.length; ++i) {
				tmpResult.queryId = doc.mentions[i].queryId;
				if (candidates[i].mids != null) {
					candidateCnt += candidates[i].mids.size();
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

					if (candidates[i].mids == null) {
						continue;
					}
					for (ByteArrayString mid : candidates[i].mids) {
						String eid = mapper.getEid(mid);
						if (eid != null && eid.equals(goldKbId)) {
							++hitCnt;
							++inKbHitCnt;
							hitFlg = true;
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

	// TODO make public
	private static LinkingResult[] getGroudTruth(String goldFileName) {
		LinkedList<LinkingResult> results = new LinkedList<LinkingResult>();

		BufferedReader reader = IOUtils.getUTF8BufReader(goldFileName);
		try {
			reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null) {
				LinkingResult result = new LinkingResult();
				result.queryId = CommonUtils.getFieldFromLine(line, 0);
				result.kbid = CommonUtils.getFieldFromLine(line, 1);
				results.add(result);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return results.toArray(new LinkingResult[results.size()]);
	}
}
