// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import dcd.el.ELConsts;
import dcd.el.dict.AliasDict;
import dcd.el.feature.FeatureLoader;
import dcd.el.feature.TfIdfExtractor;
import dcd.el.feature.TfIdfFeature;
import dcd.el.objects.Document;
import dcd.el.objects.LinkingResult;
import dcd.el.objects.Mention;
import dcd.el.tac.MidToEidMapper;

public class NaiveLinker extends LinkerWithAliasDict {

	public NaiveLinker(AliasDict dict, TfIdfExtractor tfidfExtractor,
			FeatureLoader featureLoader, MidToEidMapper mapper) {
		super(dict, mapper);
		this.tfidfExtractor = tfidfExtractor;
		this.featureLoader = featureLoader;
	}

	public void close() {
		featureLoader.close();
	}

	@Override
	public LinkingResult[] link(Document doc) {
		LinkingResult[] results = new LinkingResult[doc.mentions.length];
		TfIdfFeature tfidfFeat = tfidfExtractor.getTfIdf(doc.text);
		
		for (int i = 0; i < results.length; ++i) {			
			results[i] = linkMention(doc.mentions[i], tfidfFeat);
		}
		
		return results;
	}

	@Override
	public LinkingResult[] link14(Document doc) {
		LinkingResult[] results = link(doc);
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

	// link a mention in document
	// TODO update
	private LinkingResult linkMention(Mention mention,
			TfIdfFeature mentionTfIdfFeature) {
		LinkingResult result = new LinkingResult();
		result.queryId = mention.queryId;
		result.kbid = ELConsts.NIL;

//		LinkedList<String> mids = aliasDict.getMids(mention.nameString);
//
//		if (mids == null) {
//			return result;
//		}
//
//		long startTime = System.currentTimeMillis();
//		FeaturePack[] featurePacks = featureLoader.loadFeaturePacks(mids);
//		retrieveFeatureTime += System.currentTimeMillis() - startTime;
//
//		double curScore = -1, maxScore = -1;
//		int pos = 0;
//		for (String mid : mids) {
//			FeaturePack featurePack = featurePacks[pos++];
//			double tfidfSim = featurePack == null ? 0 : TfIdfFeature
//					.similarity(mentionTfIdfFeature, featurePack.tfidf);
//			float popularity = featurePack == null ? 0
//					: featurePack.popularity.value;
//
//			curScore = 0 * popularity + tfidfSim;
//			// System.out.println(mid + "\t" + curPop);
//			if (curScore > maxScore) {
//				maxScore = curScore;
//				result.kbid = mid;
//			}
//		}

		return result;
	}

	public long getRetrieveFeatureTime() {
		return retrieveFeatureTime;
	}

	private long retrieveFeatureTime = 0;

	private FeatureLoader featureLoader = null;
	private TfIdfExtractor tfidfExtractor = null;
}
