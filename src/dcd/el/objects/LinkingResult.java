// author: DHL brnpoem@gmail.com

package dcd.el.objects;

import java.util.Comparator;

public class LinkingResult {
	public static class ComparatorOnQueryId implements Comparator<LinkingResult> {

		@Override
		public int compare(LinkingResult lrl, LinkingResult lrr) {
			return lrl.queryId.compareTo(lrr.queryId);
		}
	}
	
	public enum EntityType {
		PER, GPE, ORG, LOC, FAC
	}
	
	public String queryId = null;
	public String kbid = null;
	public EntityType type = EntityType.PER;
	public double confidence = -1;
}
