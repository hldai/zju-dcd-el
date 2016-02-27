// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.tac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import edu.zju.dcd.edl.io.IOUtils;

public class Scorer {
	public static void score(String goldFileName, String sytemResultFileName,
			String queriesFileName,
			EidWidMapper eidWidMapper, String errorListFileName) {
		BufferedReader goldReader = IOUtils.getUTF8BufReader(goldFileName), sysReader = IOUtils
				.getUTF8BufReader(sytemResultFileName);
		
		BufferedWriter errListWriter = null;
		LinkedList<Query> queries = null;
		if (errorListFileName != null) {
			queries = QueryReader.readQueries(queriesFileName);
			errListWriter = IOUtils.getUTF8BufWriter(errorListFileName, false);
			try {
				errListWriter.write("query ID\tgold EID\tsys EID\tscore\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Iterator<Query> qiter = null;
		Query curQuery = null;
		if (queries != null) {
			qiter = queries.iterator();
			curQuery = qiter.next();
		}
		
		String sysLine = null, goldLine = null;
		int cnt = 0, correctCnt = 0;
		int inKbCnt = 0, inKbCorrectCnt = 0;
		int inKbToNilCnt = 0, nilToInKbCnt = 0;
		try {
			goldReader.readLine(); // skip first line

			while ((sysLine = sysReader.readLine()) != null) {
				goldLine = goldReader.readLine();

				if (goldLine == null) {
					System.out.println("Query id not consistent!");
					break;
				}

				String[] sysVals = sysLine.split("\t"), goldVals = goldLine
						.split("\t");

				if (!goldVals[0].equals(sysVals[0])) {
					System.out.println("Query id not consistent!");
					break;
				}

				++cnt;
				if (goldVals[1].startsWith("NIL")) {
					if (sysVals[1].startsWith("NIL"))
						++correctCnt;
					else {
						++nilToInKbCnt;
						if (errListWriter != null) {
							errListWriter.write(goldVals[0] + "\t" + goldVals[1] + "\t" + sysVals[1] + "\t" + sysVals[2]);
							
							curQuery = getQuery(curQuery, qiter, goldVals[0]);
							if (curQuery != null && curQuery.queryId.equals(goldVals[0])) {
								errListWriter.write("\t" + curQuery.name + "\t" + curQuery.docId);
							}
							
							errListWriter.write("\n");
						}
					}
				} else {
					++inKbCnt;
					if (sysVals[1].equals(goldVals[1])) {
						++inKbCorrectCnt;
						++correctCnt;
					} else {
						if (sysVals[1].startsWith("NIL"))
							++inKbToNilCnt;
						if (errListWriter != null) {
							writeErrorIds(errListWriter, goldVals[0], goldVals[1],
									sysVals[1], sysVals[2], eidWidMapper);
							
							curQuery = getQuery(curQuery, qiter, goldVals[0]);
							if (curQuery != null && curQuery.queryId.equals(goldVals[0])) {
								errListWriter.write("\t" + curQuery.name + "\t" + curQuery.docId);
							}
							
							errListWriter.write("\n");
						}
					}
				}
			}

			goldReader.close();
			sysReader.close();
			if (errListWriter != null)
				errListWriter.close();

//			System.out.println(cnt + "\t" + inKbCnt);
			System.out.println(correctCnt + "\t" + inKbCnt + "\t" + cnt);
			System.out.println("accuracy: " + (double) correctCnt / cnt);
			System.out.println("In KB accuracy: " + (double) inKbCorrectCnt
					/ inKbCnt);
			double nilAccuracy = (double) (correctCnt - inKbCorrectCnt) / (cnt - inKbCnt);
			System.out.println("NIL accuracy: " + nilAccuracy);
			int wrongCnt = cnt - correctCnt;
			System.out.println("NIL to non-NIL: " + (double)nilToInKbCnt / wrongCnt);
			System.out.println("non-NIL to NIL: " + (double)inKbToNilCnt / wrongCnt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeErrorIds(BufferedWriter writer, String qid, String goldId, 
			String sysId, String scoreVal, EidWidMapper eidWidMapper) {
		try {
			writer.write(qid + "\t" + goldId);
			if (eidWidMapper != null) {
				int goldWid = eidWidMapper.getWid(goldId);
				writer.write("_" + goldWid);
			}
			
			writer.write("\t" + sysId);
			if (eidWidMapper != null) {
				int sysWid = eidWidMapper.getWid(sysId);
				writer.write("_" + sysWid);
			}
			writer.write("\t" + scoreVal); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Query getQuery(Query curQuery, Iterator<Query> qiter, String qid) {
		if (curQuery == null || qiter == null)
			return null;
		

		int cmp = curQuery.queryId.compareTo(qid);
		if (cmp >= 0) {
			return curQuery;
		}
		
		while (qiter.hasNext()) {
			Query q = qiter.next();
			cmp = q.queryId.compareTo(qid);
			if (cmp >= 0) {
				return q;
			}
		}
		
		return null;
	}
}
