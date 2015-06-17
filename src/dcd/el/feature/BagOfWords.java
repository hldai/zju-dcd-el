package dcd.el.feature;

import java.io.StringReader;
import java.util.TreeMap;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;

public class BagOfWords {
	public static TreeMap<String, Integer> toBagOfWords(String text) {
		TreeMap<String, Integer> m = new TreeMap<String, Integer>();
		StringReader sr = new StringReader(text);
		PTBTokenizer<Word> tokenizer = PTBTokenizer.newPTBTokenizer(sr);
		while (tokenizer.hasNext()) {
			Word w = tokenizer.next();

			// TODO further process?
			String word = w.word().toLowerCase().trim();

			if (wordIllegal(word)) {
				continue;
			}

			Integer val = m.putIfAbsent(word, 1);
			if (val != null) {
				m.put(word, val + 1);
			}
		}
		
		sr.close();
		return m;
	}
	
	private static boolean wordIllegal(String word) {
		int len = word.length();
		return len == 0 || len > 25 || word.startsWith("http://")
				|| word.startsWith("https://") || word.contains("\n")
				|| word.contains("\t")
				|| (word.charAt(0) == '<' && word.charAt(len - 1) == '>');
	}
}
