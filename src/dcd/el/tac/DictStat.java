package dcd.el.tac;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

import dcd.config.IniFile;
import dcd.el.dict.AliasDict;
import dcd.el.io.IOUtils;

public class DictStat {
	public static void run(IniFile config) {

	}

	public static void genDictStat(AliasDict dict, MidToEidMapper mapper,
			String queryFileName, String goldResultFileName, String dstFileName) {
		QueryReader queryReader = new QueryReader(queryFileName);
		BufferedReader goldReader = IOUtils
				.getUTF8BufReader(goldResultFileName);
		int queryCnt = 0, candidateCnt = 0, hitCnt = 0;
		Query query = null;

		try {
			goldReader.readLine(); // skip first line

			while ((query = queryReader.nextQuery()) != null) {
				String goldLine = goldReader.readLine();

				++queryCnt;

				LinkedList<String> mids = dict.getMids(query.name);
				if (mids != null) {
					candidateCnt += mids.size();
				}

				String[] goldVals = goldLine.split("\t");
				if (goldVals[1].startsWith("NIL")) {
					++hitCnt;
				} else if (mids != null) {
					for (String mid : mids) {
						String eid = mapper.getEid(mid);
						if (eid.equals(goldVals[1])) {
							++hitCnt;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		double recall = (double)hitCnt / queryCnt,
				avgNumCandidates = (double)candidateCnt / queryCnt;
		System.out.println(queryCnt + " queries.");
		System.out.println("Recall: " + recall);
		System.out.println("Average number of candidates: " + avgNumCandidates);
	}
}
