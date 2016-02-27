// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.linker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.tac.LinkingBasisDoc;
import edu.zju.dcd.edl.tac.LinkingBasisMention;
import edu.zju.dcd.edl.tac.MidFilter;
import edu.zju.dcd.edl.tac.MidToEidMapper;

public class SimpleNaiveLinker implements SimpleLinker {	
	public SimpleNaiveLinker(MidToEidMapper mteMapper, MidFilter midFilter,
			String dstTrainingFileName) {
		this.mteMapper = mteMapper;
		this.midFilter = midFilter;
		
		if (dstTrainingFileName != null) {
			System.out.println("dst training file: " + dstTrainingFileName);
			trainFileWriter = IOUtils.getUTF8BufWriter(dstTrainingFileName, false);
		}
	}
	
	private static class TrainingEntry implements Comparable<TrainingEntry> {
		public double score;
		public double popularity, pse, tfidf, wordHitRate;
		public String kbid;
		@Override
		public int compareTo(TrainingEntry entry) {
			if (score < entry.score) {
				return 1;
			}
			
			return score == entry.score ? 0 : -1;
		}
	}
	
	private static class TrainingEntriesOfMention {
		LinkedList<TrainingEntry> trainingEntries = null;
	}

	@Override
	public LinkingResult[] link(LinkingBasisDoc linkingBasisDoc) {
		if (trainFileWriter != null)
			genCandidatesTraining(linkingBasisDoc);
		
		LinkingResult[] results = new LinkingResult[linkingBasisDoc.linkingBasisMentions.length];
		for (int i = 0; i < results.length; ++i) {
			LinkingBasisMention lbMention = linkingBasisDoc.linkingBasisMentions[i];
			
			LinkingResult result = new LinkingResult();
			result.kbid = ELConsts.NIL;
			result.queryId = lbMention.queryId;
			
			if (lbMention.numCandidates > 0) {
				double curScore = -1, maxScore = -1e5;
				for (int j = 0; j < lbMention.numCandidates; ++j) {
//					if (midFilter != null && midFilter.needFilter(lbMention.mids[j])) {
//						continue;
//					}
//					String curMid = lbMention.mids[j].toString().trim();
//					if (linkingBasisDoc.isNested[i] && curMid.equals(results[i - 1].kbid)) {
//						continue;
//					}
					
//					curScore = 1 * Math.log(lbMention.npses[j])
//							+ 6 * Math.log(lbMention.tfidfSimilarities[j] + 1e-7)
//							+ 3 * Math.log(lbMention.wordHitRates[j] + 1e-7);
					
//					curScore = 0 * Math.log(lbMention.npses[j])
//							+ 1 * Math.log(lbMention.tfidfSimilarities[j] + 1e-7)
//							+ 0 * Math.log(lbMention.wordHitRates[j] + 1e-7);
					
//					System.out.println(lbMention.npses[j] + "\t" + lbMention.tfidfSimilarities[j] + "\t" 
//					+ lbMention.wordHitRates[j] + "\t" + lbMention.docVecSimilarities[j]);

//					curScore = 1 * lbMention.npses[j];
//					curScore = 1 * lbMention.npses[j] + 1.1 * lbMention.docVecSimilarities[j];
					
//					curScore = lbMention.popularities[j] * lbMention.aliasLikelihoods[j] + 0.2 * lbMention.tfidfSimilarities[j];
//					curScore = 1 * Math.log(lbMention.popularities[j])
//							+ 10 * Math.log(lbMention.tfidfSimilarities[j]) + 1 * Math.log(lbMention.evScores[j]);
//					System.out.println(curScore + "\t" + maxScore);
					if (curScore > maxScore) {
						result.kbid = lbMention.mids[j].toString().trim();
						maxScore = curScore;
					}
				}
				result.confidence = maxScore;
//				System.out.println("link: " + maxScore);
			}
			
			if (result.kbid == ELConsts.NIL) {
				int corefIdx = linkingBasisDoc.corefChain[i];
				if (corefIdx > -1 && results[corefIdx].kbid.startsWith(ELConsts.NIL)) {
					result.kbid = results[corefIdx].kbid;
				} else {
					result.kbid = ELConsts.NIL + String.format("%05d", ++curMaxNilId);
				}
				
//				for (int j = i - 1; j > -1; --j) {
//					if (linkingBasisDoc.possibleCoref[i][j]) {
//						result.kbid = results[j].kbid;
//						break;
//					}
//				}
//				if (result.kbid == ELConsts.NIL) {
//					result.kbid = ELConsts.NIL + String.format("%05d", ++curMaxNilId);
//				}
			}
			
			results[i] = result;
		}
		
//		relink(results, linkingBasisDoc);
		
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
	
	public void closeTrainingDataWriter() {
		if (trainFileWriter != null) {
			try {
				trainFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void relink(LinkingResult[] results, LinkingBasisDoc linkingBasisDoc) {
		for (int i = 0; i < results.length; ++i) {
			int j = linkingBasisDoc.corefChain[i];
			if (j < 0)
				continue;

			if (results[j].kbid.startsWith(ELConsts.NIL)) {
				continue;
			}
			
			if (results[j].confidence > results[i].confidence) {
				results[i].kbid = results[j].kbid;
				results[i].confidence = results[j].confidence;
			}
		}
	}
	
	private void genCandidatesTraining(LinkingBasisDoc linkingBasisDoc) {
		String preKbId = null;
		
		TrainingEntriesOfMention[] trainingEntriesOfMentions = new TrainingEntriesOfMention[linkingBasisDoc.linkingBasisMentions.length];
		
		for (int i = 0; i < linkingBasisDoc.linkingBasisMentions.length; ++i) {
			LinkingBasisMention lbMention = linkingBasisDoc.linkingBasisMentions[i];
			
			if (lbMention.numCandidates > 0) {
				LinkedList<TrainingEntry> trainingEntries = new LinkedList<TrainingEntry>();
				
				double maxScore = -1e5;
				for (int j = 0; j < lbMention.numCandidates; ++j) {
					if (midFilter != null && midFilter.needFilter(lbMention.mids[j])) {
						continue;
					}
					String curMid = lbMention.mids[j].toString().trim();
					if (linkingBasisDoc.isNested[i] && preKbId != null && curMid.equals(preKbId)) {
						continue;
					}
					TrainingEntry trainingEntry = new TrainingEntry();
					trainingEntry.kbid = curMid;
					
					trainingEntry.popularity = lbMention.popularities[j];
//					trainingEntry.pse = lbMention.aliasLikelihoods[j];
					trainingEntry.pse = lbMention.npses[j];
					trainingEntry.tfidf = lbMention.tfidfSimilarities[j];
//					trainingEntry.prob = lbMention.probabilities[j];
					trainingEntry.wordHitRate = lbMention.wordHitRates[j];
//					System.out.println(Math.log(lbMention.npses[j] + 1e-7) + " " + Math.log(lbMention.tfidfSimilarities[j] + 1e-7) + " " + Math.log(lbMention.wordHitRates[j] + 1e-7));
					
					trainingEntry.score = 1 * Math.log(lbMention.npses[j] + 1e-7) 
							+ 6 * Math.log(lbMention.tfidfSimilarities[j] + 1e-7)
//							+ 0 * Math.log(lbMention.probabilities[j] + 1e-7)
							+ 3 * Math.log(lbMention.wordHitRates[j] + 1e-7);
					
//					System.out.println(trainingEntry.score);
					
					if (trainingEntry.score > maxScore) {
						maxScore = trainingEntry.score;
					}
					
					trainingEntries.add(trainingEntry);
				}
//				System.out.println(maxScore);
				
				int corefIdx = linkingBasisDoc.corefChain[i];
				boolean useCoref = false;
				if (corefIdx >= 0) {
					TrainingEntriesOfMention trainingEntriesOfMention = trainingEntriesOfMentions[corefIdx];
					if (trainingEntriesOfMention != null
							&& trainingEntriesOfMention.trainingEntries != null
							&& trainingEntriesOfMention.trainingEntries.size() > 0
							&& trainingEntriesOfMention.trainingEntries.getFirst().score > maxScore) {
						useCoref = true;
					}
				}
				
				if (useCoref) {
					trainingEntriesOfMentions[i] = trainingEntriesOfMentions[corefIdx];
					preKbId = trainingEntriesOfMentions[i].trainingEntries.getFirst().kbid;
				} else if (trainingEntries.size() > 0) {
					Collections.sort(trainingEntries);
					
					trainingEntriesOfMentions[i] = new TrainingEntriesOfMention();
					trainingEntriesOfMentions[i].trainingEntries = trainingEntries;
					preKbId = trainingEntriesOfMentions[i].trainingEntries.getFirst().kbid;
				}
			}
		}
		
		int pos = -1;
		for (TrainingEntriesOfMention trainingEntriesOfMention : trainingEntriesOfMentions) {
			++pos;
			if (trainingEntriesOfMention == null)
				continue;
			if (trainingEntriesOfMention.trainingEntries == null || trainingEntriesOfMention.trainingEntries.size() == 0)
				continue;
			
			LinkingBasisMention lbMention = linkingBasisDoc.linkingBasisMentions[pos];
			
			int cnt = 0;
			String preKbid = null;
			try {
				for (TrainingEntry entry : trainingEntriesOfMention.trainingEntries) {
					String kbid = entry.kbid;
					if (mteMapper != null) {
						kbid = mteMapper.getEid(entry.kbid);
					} else {
						kbid = "m." + kbid;
					}
					if (kbid != null && (preKbid == null || !preKbid.equals(kbid))) {
//						trainFileWriter.write(lbMention.queryId + "\t" + eid + "\t" + entry.popularity
//								+ "\t" + entry.pse + "\t" + entry.tfidf + "\t" + entry.prob + "\t" 
//								+ entry.wordHitRate + "\n");
						trainFileWriter.write(lbMention.queryId + "\t" + kbid + "\t" + entry.popularity
								+ "\t" + entry.pse + "\t" + entry.tfidf + "\t" 
								+ entry.wordHitRate + "\n");
						preKbid = kbid;
						++cnt;
					}
					if (cnt == 2)
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected MidToEidMapper mteMapper = null;
	MidFilter midFilter = null;
	
	int curMaxNilId = 0;
	
	public BufferedWriter trainFileWriter = null;
}
