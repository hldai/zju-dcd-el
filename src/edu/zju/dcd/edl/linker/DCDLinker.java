package edu.zju.dcd.edl.linker;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.tac.LinkingBasisDoc;
import edu.zju.dcd.edl.tac.LinkingBasisMention;
import edu.zju.dcd.edl.tac.MidFilter;
import edu.zju.dcd.edl.tac.MidToEidMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by dhl on 16-8-6.
 */
public class DCDLinker implements SimpleLinker {
	public DCDLinker(MidToEidMapper mteMapper) {
		this.mteMapper = mteMapper;
	}

	@Override
	public LinkingResult[] link(LinkingBasisDoc linkingBasisDoc) {
		LinkingResult[] results = new LinkingResult[linkingBasisDoc.linkingBasisMentions.length];
		for (int i = 0; i < results.length; ++i) {
			LinkingBasisMention lbMention = linkingBasisDoc.linkingBasisMentions[i];

			LinkingResult result = new LinkingResult();
			results[i] = result;
			result.kbid = ELConsts.NIL;
			result.queryId = lbMention.queryId;

			if (linkingBasisDoc.corefChain[i] > -1)
				continue;

			if (lbMention.numCandidates > 0) {
				double curScore = -1, maxScore = -1e5;
				for (int j = 0; j < lbMention.numCandidates; ++j) {
//					System.out.println(result.queryId);
					String curMid = lbMention.mids[j].toString().trim();

					if (linkingBasisDoc.isNested[i] && curMid.equals(results[i - 1].kbid)) {
						continue;
					}

					curScore = 1 * Math.log(lbMention.npses[j])
							+ 6 * Math.log(lbMention.tfidfSimilarities[j] + 1e-7)
							+ 3 * Math.log(lbMention.wordHitRates[j] + 1e-7);

					if (curScore > maxScore) {
						result.kbid = lbMention.mids[j].toString().trim();
						maxScore = curScore;
					}
				}
				result.confidence = maxScore;
//				System.out.println("link: " + maxScore);
			}

			if (result.kbid == ELConsts.NIL) {
				result.kbid = ELConsts.NIL + String.format("%05d", ++curMaxNilId);
			}
		}

		for (int i = 0; i < results.length; ++i) {
			if (linkingBasisDoc.corefChain[i] > -1) {
				results[i].kbid = results[linkingBasisDoc.corefChain[i]].kbid;
			}
		}

		return results;
	}

	@Override
	public LinkingResult[] link14(LinkingBasisDoc linkingBasisDoc) {
		LinkingResult[] results = link(linkingBasisDoc);
		for (LinkingResult result : results) {
			if (!result.kbid.startsWith(ELConsts.NIL)) {
				String eid = mteMapper.getEid(result.kbid);
				if (eid == null) {
//					result.kbid = ELConsts.NIL + result.kbid;
					result.kbid = ELConsts.NIL + String.format("%05d", ++curMaxNilId);
				} else {
					result.kbid = eid;
				}
			}
		}

		return results;
	}

	private void relink(LinkingResult[] results, LinkingBasisDoc linkingBasisDoc) {
		for (int i = 0; i < results.length; ++i) {
			int j = linkingBasisDoc.corefChain[i];
			if (j < 0)
				continue;

//			System.out.println("coref " + j);

			if (results[j].kbid.startsWith(ELConsts.NIL)) {
				continue;
			}

			if (results[j].confidence > results[i].confidence) {
				results[i].kbid = results[j].kbid;
				results[i].confidence = results[j].confidence;
			}
		}
	}

	protected MidToEidMapper mteMapper = null;

	int curMaxNilId = 0;
}
