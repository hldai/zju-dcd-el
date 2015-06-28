package dcd.el.tac;

import java.util.LinkedList;

import dcd.el.dict.AliasDict;
import dcd.el.dict.CandidatesRetriever;
import dcd.el.feature.FeatureLoader;
import dcd.el.feature.FeaturePack;
import dcd.el.feature.TfIdfExtractor;
import dcd.el.feature.TfIdfFeature;
import dcd.el.objects.ByteArrayString;
import dcd.el.objects.Document;
import dcd.el.objects.Mention;

public class LinkingBasisGen {
	public LinkingBasisGen(AliasDict dict,
			FeatureLoader featureLoader, TfIdfExtractor tfIdfExtractor) {
		this.candidatesRetriever = new CandidatesRetriever(dict);
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
			
			if (mids == null) {
				continue;
			}

			linkingBasisMention.numCandidates = mids.size();
			linkingBasisMention.mids = new ByteArrayString[mids.size()];
			linkingBasisMention.popularities = new float[mids.size()];
			linkingBasisMention.tfidfSimilarities = new double[mids.size()];
			FeaturePack[] featPacks = featureLoader.loadFeaturePacks(mids);
			int ix = 0;
			for (ByteArrayString mid : mids) {
				linkingBasisMention.mids[ix] = mid;
				if (featPacks[ix] == null) {
					linkingBasisMention.popularities[ix] = 0;
					linkingBasisMention.tfidfSimilarities[ix] = 0;
				} else {
					linkingBasisMention.popularities[ix] = featPacks[ix].popularity.value;
					linkingBasisMention.tfidfSimilarities[ix] = TfIdfFeature.similarity(
							tfIdfFeature, featPacks[ix].tfidf);
//					linkingBasisMention.tfidfSimilarities[ix] = TfIdfFeature.tfSimilarity(
//							tfIdfFeature, featPacks[ix].tfidf);
				}
				++ix;
			}
		}
		
		return result;
	}
	
	private CandidatesRetriever candidatesRetriever = null;
	private FeatureLoader featureLoader = null;
	private TfIdfExtractor tfIdfExtractor = null;
}
