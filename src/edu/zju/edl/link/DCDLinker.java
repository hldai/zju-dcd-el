package edu.zju.edl.link;

import edu.zju.edl.ELConsts;
import edu.zju.edl.obj.LinkingResult;
import edu.zju.edl.tac.MidToEidMapper;
import edu.zju.edl.feature.LinkingInfoDoc;
import edu.zju.edl.feature.LinkingInfoMention;

import java.util.Arrays;
import java.util.LinkedList;


public class DCDLinker implements LinkingInfoLinker {
	static class RankingEntry implements Comparable<RankingEntry> {
		public String kbid;
		public double score;

		public int compareTo(RankingEntry other) {
			if (this.score == other.score)
				return 0;
			return this.score > other.score ? -1 : 1;
		}
	}

	static class RankingResult {
		public String mentionId = null;
		public RankingEntry[] rankingEntries;
	}

	public DCDLinker(MidToEidMapper mteMapper, boolean toeid, boolean eidOnly) {
		this.mteMapper = mteMapper;
		this.eidOnly = eidOnly;
		this.toeid = toeid;
	}

	private RankingEntry[] rankMentionCandidates(LinkingInfoMention linkingInfoMention) {
		if (linkingInfoMention.numCandidates <= 0)
			return null;

		double curScore = -1;
//		RankingEntry[] rankingEntries = new RankingEntry[linkingInfoMention.numCandidates];
		LinkedList<RankingEntry> rankingEntries = new LinkedList<>();
		for (int j = 0; j < linkingInfoMention.numCandidates; ++j) {
			String curKbid = linkingInfoMention.mids[j].toString().trim();

			if (eidOnly) {
				String eid = mteMapper.getEid(curKbid);
				if (eid == null)
					continue;
			}

//			if (linkingInfoDoc.isNested[i] && curMid.equals(results[i - 1].kbid)) {
//				continue;
//			}

			curScore = 1 * Math.log(linkingInfoMention.commonnesses[j])
					+ 6 * Math.log(linkingInfoMention.tfidfSimilarities[j] + 1e-7)
					+ 5 * Math.log(linkingInfoMention.iwhrs[j] + 1e-7);

			RankingEntry rankingEntry = new RankingEntry();
			rankingEntry.kbid = curKbid;
			rankingEntry.score = curScore;
			rankingEntries.add(rankingEntry);
		}

		RankingEntry[] rankingEntriesArr = rankingEntries.toArray(new RankingEntry[rankingEntries.size()]);
		Arrays.sort(rankingEntriesArr);

		return rankingEntriesArr;
	}

	private RankingResult[] rankMentions(LinkingInfoDoc linkingInfoDoc) {
		RankingResult[] rankingResults = new RankingResult[linkingInfoDoc.linkingInfoMentions.length];

		for (int i = 0; i < rankingResults.length; ++i) {
			LinkingInfoMention linkingInfoMention = linkingInfoDoc.linkingInfoMentions[i];

			RankingResult rankingResult = rankingResults[i] = new RankingResult();
			rankingResult.mentionId = linkingInfoMention.mentionId;
			rankingResult.rankingEntries = null;

			if (linkingInfoDoc.corefChain[i] > -1)
				continue;

			rankingResult.rankingEntries = rankMentionCandidates(linkingInfoMention);
		}

		for (int i = 0; i < rankingResults.length; ++i) {
			if (linkingInfoDoc.corefChain[i] > -1) {
				rankingResults[i].rankingEntries = rankingResults[linkingInfoDoc.corefChain[i]].rankingEntries;
			}
		}

		return rankingResults;
	}

	private LinkingResult[] linkWithRankingResult(LinkingInfoDoc linkingInfoDoc, RankingResult[] rankingResults) {
		LinkingResult[] results = new LinkingResult[linkingInfoDoc.linkingInfoMentions.length];
		for (int i = 0; i < results.length; ++i) {
			LinkingInfoMention linkingInfoMention = linkingInfoDoc.linkingInfoMentions[i];

			LinkingResult result = new LinkingResult();
			results[i] = result;
			result.kbid = ELConsts.NIL;
			result.mentionId = linkingInfoMention.mentionId;

//			if (linkingInfoDoc.corefChain[i] > -1)
//				continue;

			RankingResult rankingResult = rankingResults[i];
			if (rankingResult.rankingEntries == null || rankingResult.rankingEntries.length == 0)
				continue;

			if (linkingInfoDoc.isNested[i]) {
				int pos = 0;
				while (pos < rankingResult.rankingEntries.length) {
					if (!rankingResult.rankingEntries[pos].kbid.equals(results[i - 1].kbid))
						break;
					++pos;
				}
				result.kbid = rankingResult.rankingEntries[pos].kbid;
			} else {
				result.kbid = rankingResult.rankingEntries[0].kbid;
			}

			if (result.kbid.equals(ELConsts.NIL)) {
				result.kbid = ELConsts.NIL + String.format("%07d", ++curMaxNilId);
			}
		}

//		for (int i = 0; i < results.length; ++i) {
//			if (linkingInfoDoc.corefChain[i] > -1) {
//				results[i].kbid = results[linkingInfoDoc.corefChain[i]].kbid;
//			}
//		}

		return results;
	}

	@Override
	public LinkingResult[] link(LinkingInfoDoc linkingInfoDoc) {
//		LinkingResult[] results = new LinkingResult[linkingInfoDoc.linkingInfoMentions.length];
		RankingResult[] rankingResults = rankMentions(linkingInfoDoc);

		return linkWithRankingResult(linkingInfoDoc, rankingResults);
	}

//	@Override
//	public LinkingResult[] link(LinkingInfoDoc linkingInfoDoc) {
//		LinkingResult[] results = new LinkingResult[linkingInfoDoc.linkingInfoMentions.length];
//		for (int i = 0; i < results.length; ++i) {
//			LinkingInfoMention lbMention = linkingInfoDoc.linkingInfoMentions[i];
//
//			LinkingResult result = new LinkingResult();
//			results[i] = result;
//			result.kbid = ELConsts.NIL;
//			result.mentionId = lbMention.mentionId;
//
//			if (linkingInfoDoc.corefChain[i] > -1)
//				continue;
//
//			if (lbMention.numCandidates > 0) {
//				double curScore = -1, maxScore = -1e5;
//				for (int j = 0; j < lbMention.numCandidates; ++j) {
////					System.out.println(result.queryId);
//					String curMid = lbMention.mids[j].toString().trim();
//
//					if (linkingInfoDoc.isNested[i] && curMid.equals(results[i - 1].kbid)) {
//						continue;
//					}
//
//					curScore = 1 * Math.log(lbMention.commonnesses[j])
//							+ 6 * Math.log(lbMention.tfidfSimilarities[j] + 1e-7)
//							+ 5 * Math.log(lbMention.iwhrs[j] + 1e-7);
//
//					if (curScore > maxScore) {
//						result.kbid = lbMention.mids[j].toString().trim();
//						maxScore = curScore;
//					}
//				}
//				result.confidence = maxScore;
////				System.out.println("link: " + maxScore);
//			}
//
//			if (result.kbid == ELConsts.NIL) {
//				result.kbid = ELConsts.NIL + String.format("%07d", ++curMaxNilId);
//			}
//		}
//
//		for (int i = 0; i < results.length; ++i) {
//			if (linkingInfoDoc.corefChain[i] > -1) {
//				results[i].kbid = results[linkingInfoDoc.corefChain[i]].kbid;
//			}
//		}
//
//		return results;
//	}

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
	private boolean eidOnly = false;
	private boolean toeid = false;
}
