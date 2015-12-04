package edu.zju.dcd.edl.wordvec;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.utils.MathUtils;

public class WordPredictor {
	public WordPredictor(String entityVecFileName, String outputVecFileName,
			String divisorsFileName, String vocabFileName, String filterWordsFileName) {
		wordVecVocab = new WordVecVocab(vocabFileName);
		loadEntityVecs(entityVecFileName);
		loadOutputVecs(outputVecFileName);
		loadDivisors(divisorsFileName);
		loadFilterWords(filterWordsFileName);
	}
	
	public static class WordProbability implements Comparable<WordProbability> {
		int wordIndex;
		float probability;
		
		@Override
		public int compareTo(WordProbability wordProbability) {
			if (probability < wordProbability.probability) {
				return 1;
			}
			return probability == wordProbability.probability ? 0 : -1;
		}
	}
	
	public Float predictText(int wid, int[] textWordIndices) {
		return predictText(wid, textWordIndices, 0, textWordIndices.length);
	}
	
	public Float predictText(int wid, int[] textWordIndices, int beg, int end) {
		int widPos = Arrays.binarySearch(wids, wid);
		if (widPos < 0)
			return null;
		
		int divisorWidPos = Arrays.binarySearch(divisorWids, wid);
		if (divisorWidPos < 0)
			return null;

		float result = 0;
		for (int i = beg; i < end; ++i) {
			result += MathUtils.dotProduct(entityVecs[widPos],
					outputVecs[textWordIndices[i]]);
		}
		
		int len = end - beg;
		
		result -= len * Math.log(divisors[divisorWidPos]);
		return result / len;
	}
	
	public int[] getWordIndices(String text) {
		StringReader sr = new StringReader(text);
		PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<>(sr,
	              new CoreLabelTokenFactory(), "ptb3Escaping=false,untokenizable=noneDelete");
		LinkedList<Integer> indices = new LinkedList<Integer>();
		while (tokenizer.hasNext()) {
			CoreLabel label = tokenizer.next();
			String word = label.value().toLowerCase();
			int pos = Arrays.binarySearch(filterWords, word);
			if (pos > 0) {
				continue;
			}
			
			int index = wordVecVocab.getWordIndex(word);
			if (index > -1) {
				indices.add(index);
			}
		}
		
		int[] indicesArr = new int[indices.size()];
		int i = 0;
		for (Integer idx : indices) {
			indicesArr[i++] = idx;
		}
		return indicesArr;
	}
	
	public void addWordIndices(String text, LinkedList<Integer> indices) {
		StringReader sr = new StringReader(text);
		PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<>(sr,
	              new CoreLabelTokenFactory(), "ptb3Escaping=false,untokenizable=noneDelete");
		while (tokenizer.hasNext()) {
			CoreLabel label = tokenizer.next();
			String word = label.value().toLowerCase();
			int pos = Arrays.binarySearch(filterWords, word);
			if (pos > 0) {
				continue;
			}
			
			int index = wordVecVocab.getWordIndex(word);
			if (index > -1) {
				indices.add(index);
			}
		}
	}
	
	public void test() {
		int wid = 43297731;
		WordProbability[] wordProbabilities = new WordProbability[outputVecs.length];
		int widPos = Arrays.binarySearch(wids, wid);
		if (widPos < 0)
			return ;
		
		int divisorWidPos = Arrays.binarySearch(divisorWids, wid);
		if (divisorWidPos < 0)
			return ;
		
		System.out.println(getProbability(entityVecs[widPos], divisors[divisorWidPos],
				"politics"));
		System.out.println(getProbability(entityVecs[widPos], divisors[divisorWidPos],
				"basketball"));
		
		float sumProbabilities = 0;
		for (int i = 0; i < outputVecs.length; ++i) {
			wordProbabilities[i] = new WordProbability();
			wordProbabilities[i].wordIndex = i;
			wordProbabilities[i].probability = getProbability(entityVecs[widPos], 
					divisors[divisorWidPos], i);
			sumProbabilities += Math.exp(wordProbabilities[i].probability);
		}
		
		System.out.println(sumProbabilities);
		Arrays.sort(wordProbabilities);
		for (int i = 0; i < 30; ++i) {
			System.out.println(wordVecVocab.getWord(wordProbabilities[i].wordIndex).toString()
					+ "\t" + wordProbabilities[i].probability);
		}
	}
	
	public float getProbability(int wid, int wordIndex) {
		int widPos = Arrays.binarySearch(wids, wid);
		if (widPos < 0)
			return -1;
		
		int divisorWidPos = Arrays.binarySearch(divisorWids, wid);
		if (divisorWidPos < 0)
			return -1;
		
		float result =  MathUtils.dotProduct(entityVecs[widPos], outputVecs[wordIndex]);
		return (float) (result - Math.log(divisors[divisorWidPos]));
	}
	
	public float getProbability(float[] entityVec, double divisor, String word) {
		int index = wordVecVocab.getWordIndex(word);
		return getProbability(entityVec, divisor, index);
	}
	
	public float getProbability(float[] entityVec, double divisor, int wordIndex) {
		float result =  MathUtils.dotProduct(entityVec, outputVecs[wordIndex]);
		return (float) (result - Math.log(divisor));
	}
	
	private void loadEntityVecs(String entityVecFileName) {
		System.out.println("Loading entity vectors...");
		
		DataInputStream dis = IOUtils.getBufferedDataInputStream(entityVecFileName);
		try {
			int numEntities = dis.readInt();
			int vecLen = dis.readInt();
			System.out.println(numEntities + " entities \t" + vecLen);
			entityVecs = new float[numEntities][];
			wids = new int[numEntities];
			for (int i = 0; i < numEntities; ++i) {
				wids[i] = dis.readInt();
				
				entityVecs[i] = new float[vecLen];
				for (int j = 0; j < vecLen; ++j) {
					entityVecs[i][j] = dis.readFloat();
				}
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done.");
	}
	
	private void loadOutputVecs(String outputVecFileName) {
		try {
			FileInputStream fis = new FileInputStream(outputVecFileName);
			FileChannel fc = fis.getChannel();
			long numVecs = IOUtils.readLittleEndianLong(fc),
					vecLen = IOUtils.readLittleEndianLong(fc);
			System.out.println(numVecs + "\t" + vecLen);
			outputVecs = new float[(int) numVecs][];
			ByteBuffer buf = ByteBuffer.allocate((int) (vecLen * Float.BYTES));
			buf.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < numVecs; ++i) {
				outputVecs[i] = new float[(int) vecLen];
				fc.read(buf);
				buf.rewind();
				buf.asFloatBuffer().get(outputVecs[i]);
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadDivisors(String divisorsFileName) {
		DataInputStream dis = IOUtils.getBufferedDataInputStream(divisorsFileName);
		try {
			int numWids = dis.readInt();
			divisorWids = new int[numWids];
			divisors = new double[numWids];
			for (int i = 0; i < numWids; ++i) {
				divisorWids[i] = dis.readInt();
				divisors[i] = dis.readDouble();
			}
			
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadFilterWords(String fileName) {
		BufferedReader reader = IOUtils.getUTF8BufReader(fileName);
		try {
			int numLines = Integer.valueOf(reader.readLine());
			filterWords = new String[numLines];
			for (int i = 0; i < numLines; ++i) {
				filterWords[i] = reader.readLine().trim();
			}
			reader.close();
			
			Arrays.sort(filterWords);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	String[] filterWords = null;
	
	int[] wids = null;
	float[][] entityVecs = null;
	
	int[] divisorWids = null;
	double[] divisors = null;
	
	WordVecVocab wordVecVocab = null;
	
	float[][] outputVecs = null;
}
