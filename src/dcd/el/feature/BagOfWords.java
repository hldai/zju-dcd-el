// author: DHL brnpoem@gmail.com

package dcd.el.feature;

import java.io.StringReader;
import java.util.TreeMap;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;

public class BagOfWords {
	private static String QUOTE_FULL_PREFIX = "<QUOTE PREVIOUSPOST=\"";
	private static String QUOTE_PREFIX = "<QUOTE";
	private static String QUOTE_SUFFIX = "\">";
	
	public static TreeMap<String, Integer> toBagOfWords(String text) {
		return toBagOfWords(text, false);
	}
	
	public static TreeMap<String, Integer> toBagOfWords(String text, boolean unwrapQuote) {
		TreeMap<String, Integer> wordCnts = new TreeMap<String, Integer>();
		StringReader sr = new StringReader(text);
		PTBTokenizer<Word> tokenizer = PTBTokenizer.newPTBTokenizer(sr);
		while (tokenizer.hasNext()) {
			Word w = tokenizer.next();
			
			if (unwrapQuote && w.word().startsWith(QUOTE_PREFIX) && w.word().endsWith(QUOTE_SUFFIX)) {
				toBagOfWordsInQuote(w.word().substring(QUOTE_FULL_PREFIX.length(), w.word().length() - QUOTE_SUFFIX.length()),
						wordCnts);
			}

			countWord(w, wordCnts);
		}
		
		sr.close();
		return wordCnts;
	}
	
	private static void toBagOfWordsInQuote(String text, TreeMap<String, Integer> wordCnts) {
		StringReader sr = new StringReader(text);
		PTBTokenizer<Word> tokenizer = PTBTokenizer.newPTBTokenizer(sr);
		while (tokenizer.hasNext()) {
			Word word = tokenizer.next();
			countWord(word, wordCnts);
		}
		sr.close();
	}
	
	private static void countWord(Word word, TreeMap<String, Integer> wordCnts) {
		// TODO further process?
		String sw = word.word().toLowerCase().trim();
		if (wordIllegal(sw)) {
			return;
		}

		Integer val = wordCnts.putIfAbsent(sw, 1);
		if (val != null) {
			wordCnts.put(sw, val + 1);
		}
	}
	
	private static boolean wordIllegal(String word) {
		int len = word.length();
		return len == 0 || len > 25 || word.startsWith("http://")
				|| word.startsWith("https://") || word.contains("\n")
				|| word.contains("\t")
				|| (word.charAt(0) == '<' && word.charAt(len - 1) == '>');
	}
}
