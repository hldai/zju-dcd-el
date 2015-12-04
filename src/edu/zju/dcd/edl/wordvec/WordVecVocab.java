package edu.zju.dcd.edl.wordvec;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;

public class WordVecVocab {
	public WordVecVocab(String vocabFileName) {
		try {
			FileInputStream fs = new FileInputStream(vocabFileName);
			FileChannel fc = fs.getChannel();
			int numWords = IOUtils.readLittleEndianInt(fc);
			System.out.println(numWords + " words in vocabulary");
			words = new ByteArrayString[numWords];
			for (int i = 0; i < numWords; ++i) {
				words[i] = new ByteArrayString();
				words[i].fromFileWithByteLen(fs);
				wordIndexMap.put(words[i], i);
//				if (i == 10252) {
//					System.out.println(words[i].toString());
//				}
			}
			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("done loading vocab.");
	}
	
	public ByteArrayString getWord(int index) {
		return words[index];
	}
	
	public int[] getWordIndices(String text, String[] filterWords) {
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
			
			int index = getWordIndex(word);
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
	
	public int getWordIndex(String word) {
		return getWordIndex(new ByteArrayString(word));
	}
	
	public int getWordIndex(ByteArrayString word) {
		Integer index = wordIndexMap.get(word);
		if (index == null)
			return -1;
		return index;
	}

	ByteArrayString[] words = null;
	TreeMap<ByteArrayString, Integer> wordIndexMap = new TreeMap<ByteArrayString, Integer>();
}
