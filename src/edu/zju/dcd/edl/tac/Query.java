// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.tac;

import java.util.Comparator;

public class Query {
	public static final String TYPE_PER = "PER";
	public static final String TYPE_GEP = "GPE";
	public static final String TYPE_ORG = "ORG";
	public static final String TYPE_LOC = "LOC";
	public static final String TYPE_FAC = "FAC";
	public static final String TYPE_TTL = "TTL";
	
	public String queryId = null;
	public String name = null;
	public String docId = null;
	public int begPos = -1;
	public int endPos = -1;
	
	public String type = null;
	
	public static class QueryComparator implements Comparator<Query> {
		@Override
		public int compare(Query ql, Query qr) {
			int docCmp = ql.docId.compareTo(qr.docId);
			if (docCmp != 0)
				return docCmp;
			
			return ql.begPos - qr.begPos;
		}
	}

	public static class QueryIdComparator implements Comparator<Query> {
		@Override
		public int compare(Query ql, Query qr) {
			return ql.queryId.compareTo(qr.queryId);
		}
	}
}
