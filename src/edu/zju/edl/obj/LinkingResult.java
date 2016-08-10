// author: DHL hldai@outlook.com

package edu.zju.edl.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;

import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.utils.CommonUtils;

public class LinkingResult {
	public static class ComparatorOnMentionId implements
			Comparator<LinkingResult> {

		@Override
		public int compare(LinkingResult lrl, LinkingResult lrr) {
			int vl = getId(lrl.mentionId), vr = getId(lrr.mentionId);
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

	public String mentionId = null;
	public String kbid = null;
	public double confidence = -1;
}
