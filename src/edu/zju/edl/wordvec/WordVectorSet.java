package edu.zju.edl.wordvec;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.obj.ByteArrayString;

public class WordVectorSet {
	
	public WordVectorSet(String fileName) {
		System.out.println("Loading word vectors...");
		try {
			DataInputStream dis = IOUtils.getBufferedDataInputStream(fileName);
			
			int numWords = dis.readInt();
			int vecLen = dis.readInt();
			
			words = new ByteArrayString[numWords];
			vectors = new float[numWords][];
			
			for (int i = 0; i < numWords; ++i) {
				words[i] = new ByteArrayString();
				words[i].fromFileWithByteLen(dis);
				
				vectors[i] = new float[vecLen];
				for (int j = 0; j < vecLen; ++j) {
					vectors[i][j] = dis.readFloat();
				}
			}
			
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	
	public int getWordVecSize() {
		return vectors[0].length;
	}
	
	public int getWordIndex(String word) {
		ByteArrayString bas = new ByteArrayString(word);
		return Arrays.binarySearch(words, bas);
	}
	
	public String getWord(int idx) {
		return words[idx].toString();
	}
	
	public float[] getVector(String word) {
		int pos = getWordIndex(word);
		if (pos < 0)
			return null;
//		System.out.println(words[pos].toString());
		return vectors[pos];
	}
	
	ByteArrayString[] words = null;
	float[][] vectors = null;
	
//	WordVectorPair[] wordVectorPairs = null;
}
