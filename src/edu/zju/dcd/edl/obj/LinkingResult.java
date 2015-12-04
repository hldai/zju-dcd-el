// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;

import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.utils.CommonUtils;

public class LinkingResult {
	public static class ComparatorOnQueryId implements
			Comparator<LinkingResult> {

		@Override
		public int compare(LinkingResult lrl, LinkingResult lrr) {
			int vl = getId(lrl.queryId), vr = getId(lrr.queryId);
			return vl - vr;
//			return lrl.queryId.compareTo(lrr.queryId);
		}
		
		private int getId(String qid) {
			int pos = qid.length() - 1;
			while (pos > -1) {
				if (Character.isDigit(qid.charAt(pos))) {
					--pos;
				} else {
					return Integer.valueOf(qid.substring(pos + 1));
				}
			}
			return 0;
		}
	}

	public static LinkingResult[] getGroudTruth(String goldFileName) {
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

	public enum EntityType {
		PER, GPE, ORG, LOC, FAC
	}

	public String queryId = null;
	public String kbid = null;
	public EntityType type = EntityType.PER;
	public double confidence = -1;
}
