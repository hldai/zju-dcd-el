package edu.zju.edl.feature;

import edu.zju.edl.cg.CandidatesDict;
import edu.zju.edl.cg.CandidatesGen;
import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.obj.Document;
import edu.zju.edl.obj.Mention;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.LinkedList;


public class LinkingInfoGen {
	public LinkingInfoGen(CandidatesGen candidatesGen, FeatureLoader featureLoader, TfIdfExtractor tfIdfExtractor) {
		this.candidatesGen = candidatesGen;
		this.featureLoader = featureLoader;
		this.tfIdfExtractor = tfIdfExtractor;
	}

	public LinkingInfoDoc getLinkingInfoDoc(Document doc) {
		LinkingInfoDoc result = new LinkingInfoDoc();
		result.docId = doc.docId;
		if (doc.mentions == null)
			return result;
		result.linkingInfoMentions = new LinkingInfoMention[doc.mentions.length];

		result.corefChain = new int[doc.mentions.length];
		CandidatesDict.CandidatesEntry[] candidatesEntries = candidatesGen.getCandidates(doc, result.corefChain);

		TfIdfFeature tfidfDoc = null;
		if (tfIdfExtractor != null)
			tfidfDoc = tfIdfExtractor.getTfIdf(doc.text);

		LinkedList<LinkingInfoMention> scoresToGet = new LinkedList<>();
		result.isNested = getNestVals(doc);
		for (int i = 0; i < doc.mentions.length; ++i) {
			LinkingInfoMention linkingInfoMention = new LinkingInfoMention();
			result.linkingInfoMentions[i] = linkingInfoMention;

			Mention mention = doc.mentions[i];
			linkingInfoMention.mentionId = mention.mentionId;

			CandidatesDict.CandidatesEntry candidatesEntry = candidatesEntries[i];
			if (candidatesEntry == null) {
				continue;
			}

			int numCandidates = candidatesEntry.mids.length;
			linkingInfoMention.numCandidates = numCandidates;
			linkingInfoMention.mids = candidatesEntry.mids;
			linkingInfoMention.commonnesses = candidatesEntry.cmns;
			linkingInfoMention.tfidfSimilarities = new double[numCandidates];
			linkingInfoMention.iwhrs = new float[numCandidates];

			scoresToGet.add(linkingInfoMention);
		}

		handleScoresToGet(scoresToGet, tfidfDoc);

		return result;
	}

	private void handleScoresToGet(LinkedList<LinkingInfoMention> scoresToGet, TfIdfFeature tfidfDoc) {
		FeaturePack[][] featurePacks = featureLoader.loadFeaturePacks(scoresToGet);
		int i = 0;
		for (LinkingInfoMention sm : scoresToGet) {
			FeaturePack[] curFeaturePacks = featurePacks[i];

			for (int j = 0; j < sm.mids.length; ++j) {
				if (curFeaturePacks == null || curFeaturePacks[j] == null || tfidfDoc == null) {
					sm.tfidfSimilarities[j] = 1e-8f;
					sm.iwhrs[j] = 1e-8f;
				} else {
					sm.tfidfSimilarities[j] = TfIdfFeature.similarity(
							tfidfDoc, curFeaturePacks[j].tfidf);
					sm.iwhrs[j] = getWordHitRateIdf(tfidfDoc, curFeaturePacks[j].tfidf);
				}
			}
			++i;
		}
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

	private float getDocVecSimilarity(float[] wikiVec, float[] docVec) {
		float norm0 = 0, norm1 = 0;
		float result = 0;

		for (int k = 0; k < docVec.length; ++k) {
			result += docVec[k] * wikiVec[k];
			norm0 += docVec[k] * docVec[k];
			norm1 += wikiVec[k] * wikiVec[k];
		}
		norm0 = (float)Math.sqrt(norm0);
		norm1 = (float)Math.sqrt(norm1);
		return result / norm0 * norm1;
	}

	private static final float KEY_WORD_IDF_THRES = 4.5f;

	private CandidatesGen candidatesGen = null;
	private FeatureLoader featureLoader = null;
	private TfIdfExtractor tfIdfExtractor = null;

	private float[][] wikiVecs = null;
	private float[][] docVecs = null;
	private int[] wids = null;
}
