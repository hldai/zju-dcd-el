package edu.zju.dcd.edl.feature;

import edu.zju.dcd.edl.cg.CandidatesDict;
import edu.zju.dcd.edl.cg.CandidatesGen;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.Mention;
import edu.zju.dcd.edl.tac.LinkingBasisDoc;
import edu.zju.dcd.edl.tac.LinkingBasisMention;
import edu.zju.dcd.edl.utils.CommonUtils;

import java.util.Arrays;

/**
 * Created by dhl on 16-7-31.
 */
public class FeatureGen {
	public FeatureGen(CandidatesGen candidatesGen, FeatureLoader featureLoader, TfIdfExtractor tfIdfExtractor) {
		this.candidatesGen = candidatesGen;
		this.featureLoader = featureLoader;
		this.tfIdfExtractor = tfIdfExtractor;
	}

	public LinkingBasisDoc getLinkingBasisDoc(Document doc) {
		LinkingBasisDoc result = new LinkingBasisDoc();
		result.docId = doc.docId;
		if (doc.mentions == null)
			return result;
		result.linkingBasisMentions = new LinkingBasisMention[doc.mentions.length];

		CandidatesDict.CandidatesEntry[] candidatesEntries = candidatesGen.getCandidatesOfMentionsInDoc(doc);

		result.corefChain = getCorefChain(doc);
		result.possibleCoref = getPossibleCoref(doc);

		TfIdfFeature tfidfDoc = null;
		if (tfIdfExtractor != null)
			tfidfDoc = tfIdfExtractor.getTfIdf(doc.text);

		result.isNested = getNestVals(doc);
		for (int i = 0; i < doc.mentions.length; ++i) {
			LinkingBasisMention linkingBasisMention = new LinkingBasisMention();
			result.linkingBasisMentions[i] = linkingBasisMention;
			Mention mention = doc.mentions[i];
			linkingBasisMention.queryId = mention.mentionId;

			CandidatesDict.CandidatesEntry candidatesEntry = candidatesEntries[i];
			if (candidatesEntry == null) {
				continue;
			}

			int numCandidates = candidatesEntry.mids.length;
			linkingBasisMention.numCandidates = numCandidates;
			linkingBasisMention.mids = candidatesEntry.mids;
			linkingBasisMention.npses = candidatesEntry.cmns;
			linkingBasisMention.tfidfSimilarities = new double[numCandidates];
			linkingBasisMention.wordHitRates = new float[numCandidates];


			FeaturePack[] featurePacks = null;
			if (featureLoader != null)
				featurePacks = featureLoader.loadFeaturePacks(candidatesEntry.mids);

			for (int j = 0; j < numCandidates; ++j) {
				if (featurePacks == null || featurePacks[j] == null || tfidfDoc == null) {
					linkingBasisMention.tfidfSimilarities[j] = 1e-8f;
					linkingBasisMention.wordHitRates[j] = 1e-8f;
				} else {
					linkingBasisMention.tfidfSimilarities[j] = TfIdfFeature.similarity(
							tfidfDoc, featurePacks[j].tfidf);
					linkingBasisMention.wordHitRates[j] = getWordHitRateIdf(tfidfDoc, featurePacks[j].tfidf);
				}
			}
		}

		return result;
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

	private boolean[] getNestVals(Document doc) {
		boolean[] isNested = new boolean[doc.mentions.length];
		isNested[0] = false;
		for (int i = 1; i < doc.mentions.length; ++i) {
			isNested[i] = (doc.mentions[i].beg >= doc.mentions[i - 1].beg
					&& doc.mentions[i].end <= doc.mentions[i - 1].end);
		}

		return isNested;
	}

	private int[] getCorefChain(Document doc) {
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

	private int getCorefIdx(int[] corefChain, int pos) {
		int idx = pos;
		while (corefChain[idx] > -1) {
			idx = corefChain[idx];
		}
		return idx;
	}

	private boolean[][] getPossibleCoref(Document doc) {
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

	private static final float KEY_WORD_IDF_THRES = 4.5f;

	private CandidatesGen candidatesGen = null;
	private FeatureLoader featureLoader = null;
	private TfIdfExtractor tfIdfExtractor = null;
}
