package edu.zju.edl.link;

import edu.zju.edl.ELConsts;
import edu.zju.edl.obj.LinkingResult;
import edu.zju.edl.tac.MidToEidMapper;
import edu.zju.edl.feature.LinkingInfoDoc;
import edu.zju.edl.feature.LinkingInfoMention;


public class DCDLinker implements LinkingInfoLinker {
	public DCDLinker(MidToEidMapper mteMapper) {
		this.mteMapper = mteMapper;
	}

	@Override
	public LinkingResult[] link(LinkingInfoDoc linkingInfoDoc) {
		LinkingResult[] results = new LinkingResult[linkingInfoDoc.linkingInfoMentions.length];
		for (int i = 0; i < results.length; ++i) {
			LinkingInfoMention lbMention = linkingInfoDoc.linkingInfoMentions[i];

			LinkingResult result = new LinkingResult();
			results[i] = result;
			result.kbid = ELConsts.NIL;
			result.mentionId = lbMention.mentionId;

			if (linkingInfoDoc.corefChain[i] > -1)
				continue;

			if (lbMention.numCandidates > 0) {
				double curScore = -1, maxScore = -1e5;
				for (int j = 0; j < lbMention.numCandidates; ++j) {
//					System.out.println(result.queryId);
					String curMid = lbMention.mids[j].toString().trim();

					if (linkingInfoDoc.isNested[i] && curMid.equals(results[i - 1].kbid)) {
						continue;
					}

					curScore = 1 * Math.log(lbMention.commonnesses[j])
							+ 6 * Math.log(lbMention.tfidfSimilarities[j] + 1e-7)
							+ 3 * Math.log(lbMention.iwhrs[j] + 1e-7);

					if (curScore > maxScore) {
						result.kbid = lbMention.mids[j].toString().trim();
						maxScore = curScore;
					}
				}
				result.confidence = maxScore;
//				System.out.println("link: " + maxScore);
			}

			if (result.kbid == ELConsts.NIL) {
				result.kbid = ELConsts.NIL + String.format("%07d", ++curMaxNilId);
			}
		}

		for (int i = 0; i < results.length; ++i) {
			if (linkingInfoDoc.corefChain[i] > -1) {
				results[i].kbid = results[linkingInfoDoc.corefChain[i]].kbid;
			}
		}

		return results;
	}

	@Override
	public LinkingResult[] link14(LinkingInfoDoc linkingInfoDoc) {
		LinkingResult[] results = link(linkingInfoDoc);
		for (LinkingResult result : results) {
			if (!result.kbid.startsWith(ELConsts.NIL)) {
				String eid = mteMapper.getEid(result.kbid);
				if (eid == null) {
//					result.kbid = ELConsts.NIL + result.kbid;
					result.kbid = ELConsts.NIL + String.format("%07d", ++curMaxNilId);
				} else {
					result.kbid = eid;
				}
			}
		}

		return results;
	}

	private MidToEidMapper mteMapper = null;

	private int curMaxNilId = 0;
}
