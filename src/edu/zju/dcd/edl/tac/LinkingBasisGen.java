package edu.zju.dcd.edl.tac;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;

import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.feature.FeatureLoader;
import edu.zju.dcd.edl.feature.FeaturePack;
import edu.zju.dcd.edl.feature.TfIdfExtractor;
import edu.zju.dcd.edl.feature.TfIdfFeature;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.Mention;
import edu.zju.dcd.edl.utils.CommonUtils;
import edu.zju.dcd.edl.utils.MathUtils;
import edu.zju.dcd.edl.utils.WidMidMapper;
import edu.zju.dcd.edl.wordvec.NameVecSimilarity;
//import edu.zju.dcd.edl.wordvec.WordVectorSet;

public class LinkingBasisGen {
	public class WordIndicesWithMentions {
		public int[] indices;
		public int[] begs;
		public int[] ends;
	}
	
	public LinkingBasisGen(CandidatesRetriever candidatesRetriever, FeatureLoader featureLoader, TfIdfExtractor tfIdfExtractor,
			WidMidMapper midWidMapper, String wikiVecsFile, String wikiWidListFile, String docVecsFile, String docIdListFile) {
		this.candidatesRetriever = candidatesRetriever;
		this.featureLoader = featureLoader;
		this.tfIdfExtractor = tfIdfExtractor;
		this.midWidMapper = midWidMapper;
		
//		wikiVecs = IOUtils.loadVectors(wikiVecsFile);
//		loadWikiIds(wikiWidListFile);
		
//		docVecs = IOUtils.loadVectors(docVecsFile);
//		loadDocIds(docIdListFile);
	}
	
	// doc's text should have been loaded
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
		
		TfIdfFeature tfIdfFeature = null;
		if (tfIdfExtractor != null)
			tfIdfFeature = tfIdfExtractor.getTfIdf(doc.text);
//		int[] textIndices = wordPredictor.getWordIndices(doc.text);
		
		float[] docVec = getDocVec(doc.docId);
		result.docVec = docVec;
		
		for (int i = 0; i < doc.mentions.length; ++i) {
			LinkingBasisMention linkingBasisMention = new LinkingBasisMention();
			result.linkingBasisMentions[i] = linkingBasisMention;
			Mention mention = doc.mentions[i];
			linkingBasisMention.queryId = mention.mentionId;
			
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
			linkingBasisMention.wordHitRates = new float[numCandidates];
			linkingBasisMention.docVecSimilarities = new float[numCandidates];
			linkingBasisMention.tmpVals = new float[numCandidates];
			
			linkingBasisMention.wikiVecs = new float[numCandidates][];

			FeaturePack[] featurePacks = null;
			if (featureLoader != null)
				featurePacks = featureLoader.loadFeaturePacks(candidates, maxNumCandidates);
			
			for (int j = 0; j < numCandidates; ++j) {
				linkingBasisMention.mids[j] = candidates[j].mid;
				linkingBasisMention.aliasLikelihoods[j] = candidates[j].likelihood;
				linkingBasisMention.popularities[j] = candidates[j].popularity;
				linkingBasisMention.npses[j] = candidates[j].npse;

//				if (doc.mentions[i].nameString.equals("Africa")) {
//					System.out.println(String.format("%s\t%.5f", candidates[j].mid.toString().trim(),
//							candidates[j].npse));
//				}

				if (featurePacks == null || featurePacks[j] == null || tfIdfFeature == null) {
					linkingBasisMention.tfidfSimilarities[j] = 1e-8f;
					linkingBasisMention.wordHitRates[j] = 1e-8f;
				} else {
					linkingBasisMention.tfidfSimilarities[j] = TfIdfFeature.similarity(
							tfIdfFeature, featurePacks[j].tfidf);
					linkingBasisMention.wordHitRates[j] = getWordHitRateIdf(tfIdfFeature, featurePacks[j].tfidf);
				}

				linkingBasisMention.docVecSimilarities[j] = 1e-8f;
				linkingBasisMention.tmpVals[j] = 1e-8f;
				float[] wikiVec = getWikiVec(candidates[j].mid.toString().trim());
				linkingBasisMention.wikiVecs[j] = wikiVec;
				if (docVec != null && wikiVec != null) {
					linkingBasisMention.docVecSimilarities[j] = getDocVecSimilarity(wikiVec, docVec);
				}
			}
		}
		
		return result;
	}
	
	private static final float KEY_WORD_IDF_THRES = 4.5f;

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
	
	private void loadWikiIds(String wikiIdFile) {
		if (wikiIdFile == null)
			return;
		
		try {
			FileInputStream fs = new FileInputStream(wikiIdFile);
			FileChannel fc = fs.getChannel();
			int numWids = IOUtils.readLittleEndianInt(fc);
			System.out.println(numWids + " wids");
			
			wids = new int[numWids];
			
			ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES * numWids);
			buf.order(ByteOrder.LITTLE_ENDIAN);
			fc.read(buf);
			buf.rewind();

			buf.asIntBuffer().get(wids);
			
			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadDocIds(String docIdFile) {
		if (docIdFile == null)
			return;
		
		BufferedReader reader = IOUtils.getUTF8BufReader(docIdFile);
		docIdToIdx = new HashMap<String, Integer>();
		
		try {
			String line = null;
			int cnt = 0;
			while ((line = reader.readLine()) != null) {
				String docId = line;
				if (docId.endsWith(".xml"))
					docId = docId.substring(0, docId.length() - 4);
				if (docId.endsWith(".df"))
					docId = docId.substring(0, docId.length() - 3);
				if (docId.endsWith(".nw"))
					docId = docId.substring(0, docId.length() - 3);

				docIdToIdx.put(docId, cnt);
				++cnt;
			}
			reader.close();
			System.out.println(cnt + " docs read from " + docIdFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private float[] getWikiVec(String mid) {
		if (wikiVecs == null)
			return null;
		
		int wid = midWidMapper.getWid(mid);
		if (wid < 0)
			return null;
		
		int pos = Arrays.binarySearch(wids, wid);
		if (pos < 0)
			return null;
		return wikiVecs[pos];
	}
	
	private float[] getDocVec(String docId) {
		if (docVecs == null)
			return null;
		
		if (docId.endsWith(".xml"))
			docId = docId.substring(0, docId.length() - 4);
		if (docId.endsWith(".df"))
			docId = docId.substring(0, docId.length() - 3);
		if (docId.endsWith(".nw"))
			docId = docId.substring(0, docId.length() - 3);
		
		Integer idx = docIdToIdx.get(docId);
		if (idx == null) {
			System.out.println("doc not found " + docId);
			return null;
		}
		
		return docVecs[idx];
	}
	
	private CandidatesRetriever candidatesRetriever = null;
	private FeatureLoader featureLoader = null;
	private TfIdfExtractor tfIdfExtractor = null;
	// for finding wiki vecs
	private WidMidMapper midWidMapper = null;
	
	private float[][] wikiVecs = null;
	private float[][] docVecs = null;
	private int[] wids = null;
	private HashMap<String, Integer> docIdToIdx = null;
}
