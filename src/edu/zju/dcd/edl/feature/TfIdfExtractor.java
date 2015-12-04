// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.feature;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;

public class TfIdfExtractor {
	public static class WordCount implements Comparable<WordCount> {
		public int index = -1;
		public int count = -1;
		
		@Override
		public int compareTo(WordCount wcr) {
			return index - wcr.index;
		}
	}
	
	private static class CmpTmp implements Comparable<CmpTmp> {
		ByteArrayString term;
		double idf;
		
		public CmpTmp(ByteArrayString term, double idf) {
			this.term = term;
			this.idf = idf;
		}

		@Override
		public int compareTo(CmpTmp cmpTmp) {
			if (idf < cmpTmp.idf)
				return -1;
			return idf == cmpTmp.idf ? 0 : 1;
		}
	}
	
	public TfIdfExtractor(String idfFileName) {
		System.out.println("Loading idf file...");
		DataInputStream dis = IOUtils.getBufferedDataInputStream(idfFileName);
		
		try {
			int numTerms = dis.readInt();
//			CmpTmp[] cmpTmps = new CmpTmp[numTerms];
			terms = new ByteArrayString[numTerms];
			idfs = new double[numTerms];
			for (int i = 0; i < numTerms; ++i) {
				terms[i] = new ByteArrayString();
				terms[i].fromFileWithByteLen(dis);
				idfs[i] = dis.readDouble();
				
//				cmpTmps[i] = new CmpTmp(terms[i], idfs[i]);
			}
			
			dis.close();
			System.out.println("Done.");
			
//			Arrays.sort(cmpTmps);
//			for (int i = 0; i < cmpTmps.length; ++i) {
////				if (cmpTmps[i].term.toString().equals("chicago")) {
////					System.out.println(cmpTmps[i].term.toString() + "\t" + cmpTmps[i].idf);
////				}
//				if (cmpTmps[i].idf > 4.5 && cmpTmps[i].idf < 5)
//					System.out.println(cmpTmps[i].term.toString() + "\t" + cmpTmps[i].idf);
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TfIdfFeature getTfIdf(String text) {
		if (text == null)
			return null;
		
		TreeMap<String, Integer> termCntsMap = BagOfWords.toBagOfWords(text);

		int docTermCnt = 0;
		TfIdfFeature feat = new TfIdfFeature();
		int[] termIndices = new int[termCntsMap.size()];
		int[] termCnts = new int[termCntsMap.size()];
		int cnt = 0;
		ByteArrayString bas = new ByteArrayString();
		for (Map.Entry<String, Integer> entry : termCntsMap.entrySet()) {
			docTermCnt += entry.getValue();
			
			bas.fromString(entry.getKey());
			int pos = Arrays.binarySearch(terms, bas);
//			System.out.println(entry.getKey() + "\t" + pos);
			if (pos > -1) {
				termIndices[cnt] = pos;
				termCnts[cnt] = entry.getValue();
				++cnt;
			}
		}

		feat.termIndices = Arrays.copyOf(termIndices, cnt);
//		feat.values = new double[cnt];
		feat.tfs = new float[cnt];
		feat.idfs = new float[cnt];
		for (int i = 0; i < cnt; ++i) {
//			System.out.println(termCnts[i] + "\t" + docTermCnt + "\t" + idfs[termIndices[i]]);
//			feat.values[i] = (double) termCnts[i] / docTermCnt * idfs[termIndices[i]];
			feat.tfs[i] = (float) termCnts[i] / docTermCnt;
			feat.idfs[i] = (float) idfs[termIndices[i]];
		}

		return feat;
	}

	public TfIdfFeature getTfIdf(int numTerms, int[] termIndices, int[] termCnts,
			int numDocTerms) {
		TfIdfFeature feature = new TfIdfFeature();
		feature.termIndices = termIndices;
		for (int i = 0; i < numTerms; ++i) {
//			feature.values[i] = (double)termCnts[i] / numDocTerms * idfs[termIndices[i]];
			feature.tfs[i] = (float) termCnts[i] / numDocTerms;
			feature.idfs[i] = (float) idfs[termIndices[i]];
		}
		return feature;
	}
	
	public TfIdfFeature getTfIdf(WordCount[] wordCounts, int numWords, int numDocTerms) {
		TfIdfFeature feature = new TfIdfFeature();
		feature.termIndices = new int[numWords];
//		feature.values = new double[numWords];
		feature.tfs = new float[numWords];
		feature.idfs = new float[numWords];
		for (int i = 0; i < numWords; ++i) {
			feature.termIndices[i] = wordCounts[i].index;
//			System.out.println(wordCounts[i].count + " " + idfs[wordCounts[i].index]);
//			feature.values[i] = (double)wordCounts[i].count / numDocTerms * idfs[wordCounts[i].index];
			feature.tfs[i] = (float)wordCounts[i].count / numDocTerms;
			feature.idfs[i] = (float)idfs[wordCounts[i].index];
		}
		return feature;
	}
	
	public int getTermIndex(String term) {
		return Arrays.binarySearch(terms, new ByteArrayString(term));
	}
	
	private ByteArrayString[] terms = null;
	private double[] idfs = null;
}
