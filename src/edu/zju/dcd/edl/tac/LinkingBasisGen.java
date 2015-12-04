package edu.zju.dcd.edl.tac;

import java.util.Arrays;
import java.util.LinkedList;

import dcd.el.feature.FeatureLoader;
import dcd.el.feature.FeaturePack;
import dcd.el.feature.TfIdfExtractor;
import dcd.el.feature.TfIdfFeature;
import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.Mention;
import edu.zju.dcd.edl.utils.CommonUtils;
import edu.zju.dcd.edl.utils.WidMidMapper;
import edu.zju.dcd.edl.wordvec.NameVecSimilarity;
import edu.zju.dcd.edl.wordvec.WordPredictor;
import edu.zju.dcd.edl.wordvec.WordVectorSet;

public class LinkingBasisGen {
	public class WordIndicesWithMentions {
		public int[] indices;
		public int[] begs;
		public int[] ends;
	}
	
	public LinkingBasisGen(CandidatesRetriever candidatesRetriever,
			FeatureLoader featureLoader, TfIdfExtractor tfIdfExtractor, WordPredictor wordPredictor,
			WidMidMapper midWidMapper) {
		this.candidatesRetriever = candidatesRetriever;
		this.featureLoader = featureLoader;
		this.tfIdfExtractor = tfIdfExtractor;
		this.wordPredictor = wordPredictor;
		this.midWidMapper = midWidMapper;
		
		// TODO
		WordVectorSet wordVectorSet = new WordVectorSet("e:/el/word2vec/wiki_vectors.jbin");
		nameVecSimilarity = new NameVecSimilarity("e:/el/vec_rep/name_vecs.bin", wordVectorSet);
	}
	
	// doc's text should be loaded
	public LinkingBasisDoc getLinkingBasisDoc(Document doc, int maxNumCandidates) {
		LinkingBasisDoc result = new LinkingBasisDoc();
		result.docId = doc.docId;
		if (doc.mentions == null)
			return result;
		result.linkingBasisMentions = new LinkingBasisMention[doc.mentions.length];
		
		result.isNested = getNestVals(doc);
		
		CandidatesRetriever.CandidatesOfMention[] candidatesOfMentions = candidatesRetriever.getCandidatesInDocument(doc);
		
		result.corefChain = getCorefChain(doc, candidatesOfMentions);
		result.possibleCoref = getPossibleCoref(doc, candidatesOfMentions);
		
		TfIdfFeature tfIdfFeature = tfIdfExtractor.getTfIdf(doc.text);
//		int[] textIndices = wordPredictor.getWordIndices(doc.text);
		
		WordIndicesWithMentions indicesWithMentions = wordPredictor == null 
				? null : getIndicesWithMentions(doc.text, doc.mentions);
		
		for (int i = 0; i < doc.mentions.length; ++i) {
			LinkingBasisMention linkingBasisMention = new LinkingBasisMention();
			result.linkingBasisMentions[i] = linkingBasisMention;
			Mention mention = doc.mentions[i];
			linkingBasisMention.queryId = mention.queryId;
			
			CandidatesRetriever.CandidateWithPopularity[] candidates = candidatesOfMentions[i].candidates;
			
			if (candidates == null) {
				continue;
			}

			int numCandidates = maxNumCandidates < candidates.length ? maxNumCandidates : candidates.length;
			
			linkingBasisMention.numCandidates = numCandidates;
			linkingBasisMention.mids = new ByteArrayString[numCandidates];
			linkingBasisMention.aliasLikelihoods = new float[numCandidates];
			linkingBasisMention.popularities = new float[numCandidates];
			linkingBasisMention.npses = new float[numCandidates];
			linkingBasisMention.tfidfSimilarities = new double[numCandidates];
			linkingBasisMention.probabilities = new float[numCandidates];
			linkingBasisMention.wordHitRates = new float[numCandidates];
			
			FeaturePack[] featurePacks = featureLoader.loadFeaturePacks(candidates, maxNumCandidates);
			
			int indicesBeg = 0, indicesEnd = 0;
			if (wordPredictor != null) {
				indicesBeg = indicesWithMentions.begs[i] - 5;
				indicesEnd = indicesWithMentions.ends[i] + 5;
				if (indicesBeg < 0)
					indicesBeg = 0;
				if (indicesEnd >= indicesWithMentions.indices.length)
					indicesEnd = indicesWithMentions.indices.length - 1;
			}
			
			for (int j = 0; j < numCandidates; ++j) {
				linkingBasisMention.mids[j] = candidates[j].mid;
				linkingBasisMention.aliasLikelihoods[j] = candidates[j].likelihood;
				linkingBasisMention.popularities[j] = candidates[j].popularity;
				linkingBasisMention.npses[j] = candidates[j].npse;
				if (featurePacks[j] == null) {
					linkingBasisMention.tfidfSimilarities[j] = 0;
					linkingBasisMention.wordHitRates[j] = 0;
				} else {
					linkingBasisMention.tfidfSimilarities[j] = TfIdfFeature.similarity(
							tfIdfFeature, featurePacks[j].tfidf);
//					linkingBasisMention.wordHitRates[j] = getWordHitRate(tfIdfFeature, featurePacks[j].tfidf);
					linkingBasisMention.wordHitRates[j] = getWordHitRateIdf(tfIdfFeature, featurePacks[j].tfidf);
//					linkingBasisMention.tfidfSimilarities[j] = TfIdfFeature.tfSimilarity(
//							tfIdfFeature, featurePacks[j].tfidf);
				}

				linkingBasisMention.probabilities[j] = -1e8f;
				
				if (wordPredictor != null) {
					int wid = midWidMapper.getWid(candidates[j].mid.toString().trim());
					if (wid > -1) {
	//					Float probability = wordPredictor.predictText(wid, indicesWithMentions.indices,
	//							indicesBeg, indicesEnd);
						Float probability = wordPredictor.predictText(wid, indicesWithMentions.indices);
						if (probability != null) {
							linkingBasisMention.probabilities[j] = probability;
						}
					}
				} else if (nameVecSimilarity != null) {
					linkingBasisMention.probabilities[j] = nameVecSimilarity.getSimilarity(mention.nameString,
							candidates[j].mid);
				}
//				linkingBasisMention.probabilities[j] = (float) Math.random();
				
			}
		}
		
		return result;
	}
	
	private static final float KEY_WORD_IDF_THRES = 4.5f;
	private float getWordHitRate(TfIdfFeature docTfIdf, TfIdfFeature candidateTfIdf) {
		int hitCnt = 0, keywordCnt = 0;
		int candTermIdxPos = 0;
		for (int i = 0; i < docTfIdf.termIndices.length; ++i) {
			if (docTfIdf.idfs[i] > KEY_WORD_IDF_THRES) {
				keywordCnt += 1;
				while (candTermIdxPos < candidateTfIdf.termIndices.length 
						&& candidateTfIdf.termIndices[candTermIdxPos] < docTfIdf.termIndices[i]) {
					++candTermIdxPos;
				}
				
				if (candTermIdxPos < candidateTfIdf.termIndices.length 
						&& candidateTfIdf.termIndices[candTermIdxPos] == docTfIdf.termIndices[i]) {
					++hitCnt;
				}
			}
		}
		
		return (float)hitCnt / keywordCnt;
	}

	private float getWordHitRateIdf(TfIdfFeature docTfIdf, TfIdfFeature candidateTfIdf) {
		float idfSum = 0, hitSum = 0;
		int candTermIdxPos = 0;
		for (int i = 0; i < docTfIdf.termIndices.length; ++i) {
			if (docTfIdf.idfs[i] > KEY_WORD_IDF_THRES) {
				idfSum += docTfIdf.idfs[i];
				while (candTermIdxPos < candidateTfIdf.termIndices.length 
						&& candidateTfIdf.termIndices[candTermIdxPos] < docTfIdf.termIndices[i]) {
					++candTermIdxPos;
				}
				
				if (candTermIdxPos < candidateTfIdf.termIndices.length 
						&& candidateTfIdf.termIndices[candTermIdxPos] == docTfIdf.termIndices[i]) {
					hitSum += docTfIdf.idfs[i];
				}
			}
		}
		
		return hitSum / idfSum;
	}

	private float getWordHitRateIdfSim(TfIdfFeature docTfIdf, TfIdfFeature candidateTfIdf) {
		TfIdfFeature fl = docTfIdf, fr = candidateTfIdf;
		if (fl.termIndices == null || fr.termIndices == null) {
			return 0;
		}

		double result = 0, tmp = 0;
		int posl = 0, posr = 0;
		while (posl < fl.tfs.length && posr < fr.tfs.length) {
			int terml = fl.termIndices[posl], termr = fr.termIndices[posr];
			if (terml < termr) {
				++posl;
			} else if (terml > termr) {
				++posr;
			} else {
				if (fl.idfs[posl] > KEY_WORD_IDF_THRES) {
					tmp = fl.tfs[posl] * fr.tfs[posr];
					tmp *= fl.idfs[posl] * fr.idfs[posr];
					result += tmp;
				}
				++posl;
				++posr;
			}
		}
		
		result /= fl.getNorm(true) * fr.getNorm(true);

		return (float)result;
	}
	
	private boolean[] getNestVals(Document doc) {
		boolean[] isNested = new boolean[doc.mentions.length];
		isNested[0] = false;
		for (int i = 1; i < doc.mentions.length; ++i) {
			isNested[i] = (doc.mentions[i].beg >= doc.mentions[i - 1].beg
					&& doc.mentions[i].end <= doc.mentions[i - 1].end);
		}
		
		return isNested;
	}
	
	private int[] getCorefChain(Document doc, CandidatesRetriever.CandidatesOfMention[] candidatesOfMentions) {
		int[] corefChain = new int[doc.mentions.length];
		Arrays.fill(corefChain, -1);
		
		for (int i = 0; i < doc.mentions.length; ++i) {
			String curNameString = doc.mentions[i].nameString;
			
			for (int j = i - 1; j > -1; --j) {
				if (doc.mentions[i].beg >= doc.mentions[j].beg 
						&& doc.mentions[i].end <= doc.mentions[j].end) {
					continue;
				}
				
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)
						|| CommonUtils.isAbbr(doc.mentions[j].nameString, curNameString)) {
					corefChain[i] = getCorefIdx(corefChain, j);
				}
			}
		}
		
		return corefChain;
	}
	
	private boolean[][] getPossibleCoref(Document doc, CandidatesRetriever.CandidatesOfMention[] candidatesOfMentions) {
		int numMentions = doc.mentions.length;
		boolean[][] coref = new boolean[numMentions][numMentions];
		for (int i = 1; i < numMentions; ++i) {
			String curNameString = doc.mentions[i].nameString;
			for (int j = 0; j < i; ++j) {
				if (doc.mentions[i].beg >= doc.mentions[j].beg 
						&& doc.mentions[i].end <= doc.mentions[j].end) {
					continue;
				}
				
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)
						|| CommonUtils.isAbbr(doc.mentions[j].nameString, curNameString)) {
					coref[i][j] = true;
				}
			}
		}
		
		return coref;
	}
	
	private int getCorefIdx(int[] corefChain, int pos) {
		int idx = pos;
		while (corefChain[idx] > -1) {
			idx = corefChain[idx];
		}
		return idx;
	}
	
	private WordIndicesWithMentions getIndicesWithMentions(String text, Mention[] mentions) {
		WordIndicesWithMentions result = new WordIndicesWithMentions();
		result.begs = new int[mentions.length];
		result.ends = new int[mentions.length];
		
		String textPart = null;
		int curPos = 0;
		LinkedList<Integer> indices = new LinkedList<Integer>();
		for (int i = 0; i < mentions.length; ++i) {
//			System.out.println("beg " + mentions[i].beg);
			if (curPos >= mentions[i].beg) {
				result.begs[i] = result.begs[i - 1];
				result.ends[i] = result.ends[i - 1];
				continue;
			}
			
			textPart = text.substring(curPos, mentions[i].beg);
			wordPredictor.addWordIndices(textPart, indices);
			result.begs[i] = indices.size();
			
			textPart = text.substring(mentions[i].beg, mentions[i].end + 1);			
			wordPredictor.addWordIndices(textPart, indices);
			result.ends[i] = indices.size() - 1;
			
			curPos = mentions[i].end + 1;
		}
		textPart = text.substring(curPos);
		wordPredictor.addWordIndices(textPart, indices);
		
		result.indices = new int[indices.size()];
		int pos = 0;
		for (Integer idx : indices) {
			result.indices[pos++] = idx;
		}
		
		return result;
	}
	
	private CandidatesRetriever candidatesRetriever = null;
	private FeatureLoader featureLoader = null;
	private TfIdfExtractor tfIdfExtractor = null;
	private WordPredictor wordPredictor = null;
	private WidMidMapper midWidMapper = null;
	private NameVecSimilarity nameVecSimilarity = null;
}
