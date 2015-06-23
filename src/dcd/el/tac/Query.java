// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.util.Comparator;

public class Query {
	public String queryId = null;
	public String name = null;
	public String docId = null;
	public int begPos = 0;
	public int endPos = 0;
	
	public static class QueryComparator implements Comparator<Query> {

		@Override
		public int compare(Query ql, Query qr) {
			int docCmp = ql.docId.compareTo(qr.docId);
			if (docCmp != 0)
				return docCmp;
			
			return ql.begPos - qr.begPos;
		}
	}
}
