// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.obj;

import java.util.Comparator;

public class Mention {
	public String queryId = null;
	public String nameString = null;
	public int beg = -1;
	public int end = -1;
	
	public static class MentionPosComparator implements Comparator<Mention> {
		@Override
		public int compare(Mention m0, Mention m1) {
			if (m0.beg != m1.beg)
				return m0.beg - m1.beg;
			
			return m1.end - m0.end;
		}
	}
}
