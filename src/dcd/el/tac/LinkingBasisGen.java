package dcd.el.tac;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import dcd.el.dict.CandidatesRetriever;
import dcd.el.feature.FeatureLoader;
import dcd.el.feature.FeaturePack;
import dcd.el.feature.TfIdfExtractor;
import dcd.el.feature.TfIdfFeature;
import dcd.el.objects.ByteArrayString;
import dcd.el.objects.Document;
import dcd.el.objects.Mention;

public class LinkingBasisGen {
	public LinkingBasisGen(CandidatesRetriever candidatesRetriever,
			FeatureLoader featureLoader, TfIdfExtractor tfIdfExtractor) {
		this.candidatesRetriever = candidatesRetriever;
		this.featureLoader = featureLoader;
		this.tfIdfExtractor = tfIdfExtractor;
	}
	
	// doc's text should be loaded
	public LinkingBasisDoc getLinkingBasisDoc(Document doc) {
		LinkingBasisDoc result = new LinkingBasisDoc();
		result.docId = doc.docId;
		if (doc.mentions == null)
			return result;
		result.linkingBasisMentions = new LinkingBasisMention[doc.mentions.length];
		
		CandidatesRetriever.Candidates[] candidates = candidatesRetriever.getCandidatesInDocument(doc);
		TfIdfFeature tfIdfFeature = tfIdfExtractor.getTfIdf(doc.text);
		for (int i = 0; i < doc.mentions.length; ++i) {
			LinkingBasisMention linkingBasisMention = new LinkingBasisMention();
			result.linkingBasisMentions[i] = linkingBasisMention;
			Mention mention = doc.mentions[i];
			linkingBasisMention.queryId = mention.queryId;
			
			LinkedList<ByteArrayString> mids = candidates[i].mids;
			LinkedList<Float> aliasLikelihoods = candidates[i].pses;
			
			if (mids == null) {
				continue;
			}

			linkingBasisMention.numCandidates = mids.size();
			linkingBasisMention.mids = new ByteArrayString[mids.size()];
			linkingBasisMention.aliasLikelihoods = new float[mids.size()];
			linkingBasisMention.popularities = new float[mids.size()];
			linkingBasisMention.tfidfSimilarities = new double[mids.size()];
			linkingBasisMention.evScores = new double[mids.size()];
			FeaturePack[] featurePacks = featureLoader.loadFeaturePacks(mids);
			HashMap<Integer, Float> tfSums = getEvWeights(featurePacks);
			int ix = 0;
			Iterator<ByteArrayString> midIterator = mids.iterator();
			Iterator<Float> aliasLikelihoodIterator = aliasLikelihoods == null ? null : aliasLikelihoods.iterator();
			while (midIterator.hasNext()) {
				linkingBasisMention.mids[ix] = midIterator.next();
				linkingBasisMention.aliasLikelihoods[ix] = aliasLikelihoodIterator == null ? 0 : aliasLikelihoodIterator.next();
				if (featurePacks[ix] == null) {
					linkingBasisMention.popularities[ix] = 0;
					linkingBasisMention.tfidfSimilarities[ix] = 0;
					linkingBasisMention.evScores[ix] = 0;
				} else {
					linkingBasisMention.popularities[ix] = featurePacks[ix].popularity.value;
					linkingBasisMention.tfidfSimilarities[ix] = TfIdfFeature.similarity(
							tfIdfFeature, featurePacks[ix].tfidf);
					linkingBasisMention.evScores[ix] = getEvScore(tfIdfFeature, featurePacks[ix].tfidf, tfSums);
//					linkingBasisMention.tfidfSimilarities[ix] = TfIdfFeature.tfSimilarity(
//							tfIdfFeature, featPacks[ix].tfidf);
				}
				++ix;
			}
			
			double evSum = 0;
			for (double score : linkingBasisMention.evScores)
				evSum += score;
			if (Math.abs(evSum) > 0.0001) {
				for (ix = 0; ix < mids.size(); ++ix) {
					linkingBasisMention.evScores[ix] /= evSum;
				}
			}
		}
		
		return result;
	}
	
	private HashMap<Integer, Float> getEvWeights(FeaturePack[] featurePacks) {
		HashMap<Integer, Float> weights = new HashMap<Integer, Float>();
		for (int i = 0; i < featurePacks.length; ++i) {
			if (featurePacks[i] == null)
				continue;
			TfIdfFeature tfIdfFeature = featurePacks[i].tfidf;
			if (tfIdfFeature.tfs == null)
				continue;
			for (int j = 0; j < tfIdfFeature.tfs.length; ++j) {
				int termIdx = tfIdfFeature.termIndices[j];
				Float val = weights.get(termIdx);
				if (val == null) {
					weights.put(termIdx, tfIdfFeature.tfs[j]);
				} else {
					weights.put(termIdx, val + tfIdfFeature.tfs[j]);
				}
			}
		}
		
		return weights;
	}
	
	private double getEvScore(TfIdfFeature fdoc, TfIdfFeature fcand, HashMap<Integer, Float> weights) {
		if (fdoc.termIndices == null || fcand.termIndices == null)
			return 0;
		
		double result = 0;
		int posl = 0, posr = 0;
		while (posl < fdoc.tfs.length && posr < fcand.tfs.length) {
			int terml = fdoc.termIndices[posl], termr = fcand.termIndices[posr];
			if (terml < termr) {
				++posl;
			} else if (terml > termr) {
				++posr;
			} else {
				Float weight = weights.get(terml);
				if (weight == null) {
					System.err.println("Error getting ev score: term index not found.");
				} else {
//					result += fdoc.idfs[posl] * fcand.tfs[posr] / weight;
					result += fcand.tfs[posr] / weight;
				}
				++posl;
				++posr;
			}
		}
		
		return result;
	}
	
	private CandidatesRetriever candidatesRetriever = null;
	private FeatureLoader featureLoader = null;
	private TfIdfExtractor tfIdfExtractor = null;
}
