// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import dcd.el.ELConsts;
import dcd.el.objects.LinkingResult;
import dcd.el.tac.LinkingBasisDoc;
import dcd.el.tac.LinkingBasisMention;
import dcd.el.tac.MidToEidMapper;

public class SimpleNaiveLinker implements SimpleLinker {	
	public SimpleNaiveLinker(MidToEidMapper mteMapper) {
		this.mteMapper = mteMapper;
	}

	@Override
	public LinkingResult[] link(LinkingBasisDoc linkingBasisDoc) {
		LinkingResult[] results = new LinkingResult[linkingBasisDoc.linkingBasisMentions.length];
		for (int i = 0; i < results.length; ++i) {
			LinkingBasisMention lbMention = linkingBasisDoc.linkingBasisMentions[i];
			
			LinkingResult result = new LinkingResult();
			result.kbid = ELConsts.NIL;
			result.queryId = lbMention.queryId;
			
			if (lbMention.numCandidates > 0) {
				double curScore = -1, maxScore = -1e5;
				for (int j = 0; j < lbMention.numCandidates; ++j) {
					curScore = Math.log(lbMention.popularities[j]) + 10 * Math.log(lbMention.tfidfSimilarities[j]);
//					System.out.println(mc.popularities[j] + "\t" + mc.tfidfSimilarities[j] + "\t" + curScore + "\t" + maxScore);
					if (curScore > maxScore) {
						result.kbid = lbMention.mids[j].toString().trim();
						maxScore = curScore;
					}
				}
				result.confidence = maxScore;
			}
			
			results[i] = result;
		}
		
		return results;
	}
	
	@Override
	public LinkingResult[] link14(LinkingBasisDoc linkingBasisDoc) {
		LinkingResult[] results = link(linkingBasisDoc);
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
