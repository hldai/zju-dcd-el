package dcd.el.linker;

import dcd.el.ELConsts;
import dcd.el.objects.LinkingResult;
import dcd.el.tac.DocMentionCandidates;
import dcd.el.tac.MentionCandidates;
import dcd.el.tac.MidToEidMapper;

public class SimpleNaiveLinker {	
	public SimpleNaiveLinker(MidToEidMapper mteMapper) {
		this.mteMapper = mteMapper;
	}

	public LinkingResult[] link(DocMentionCandidates dmc) {
		LinkingResult[] results = new LinkingResult[dmc.mentionsCandidates.length];
		for (int i = 0; i < results.length; ++i) {
			MentionCandidates mc = dmc.mentionsCandidates[i];
			
			LinkingResult result = new LinkingResult();
			result.kbid = ELConsts.NIL;
			result.queryId = mc.queryId;
			
			if (mc.numCandidates > 0) {
				double curScore = -1, maxScore = -1000;
				for (int j = 0; j < mc.numCandidates; ++j) {
					curScore = Math.log(mc.popularities[j]) + 5 * Math.log(mc.tfidfSimilarities[j]);
//					System.out.println(mc.popularities[j] + "\t" + mc.tfidfSimilarities[j] + "\t" + curScore + "\t" + maxScore);
					if (curScore > maxScore) {
						result.kbid = mc.mids[j];
						maxScore = curScore;
					}
				}
			}
			
			results[i] = result;
		}
		
		return results;
	}
	
	public LinkingResult[] link14(DocMentionCandidates dmc) {
		LinkingResult[] results = link(dmc);
		for (LinkingResult result : results) {
			if (!result.kbid.equals(ELConsts.NIL)) {
				result.kbid = mteMapper.getEid(result.kbid);
				if (result.kbid == null) {
					result.kbid = ELConsts.NIL;
				}
			}
		}

		return results;
	}
	
	
	protected MidToEidMapper mteMapper = null;
}
