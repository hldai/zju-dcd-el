package edu.zju.edl.prep;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;

/**
 * Created by dhl on 16-8-2.
 * do pos tagging, tokenization
 */
public class TextPos {
	public static List<List<TaggedWord>> tagText(String text, MaxentTagger tagger) {
		StringReader stringReader = new StringReader(text);
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(stringReader);

		List<List<TaggedWord>> taggedSentences = new LinkedList<List<TaggedWord>>();
		for (List<HasWord> sentence : sentences) {
			List<TaggedWord> taggedWords = tagger.apply(sentence);
//			for (TaggedWord w : taggedWords) {
////				System.out.println(String.format("%s\t%s\t%d\t%d", w.value(), w.tag(), w.beginPosition(), w.endPosition()));
//				System.out.println(String.format("%s\t%s", text.substring(w.beginPosition(), w.endPosition()), w.tag()));
//			}
			taggedSentences.add(taggedWords);
		}

		return taggedSentences;
	}

	private static void handleNextTextBlock(MaxentTagger tagger, BufferedReader textFileReader,
											BufferedWriter dstFileWriter) throws IOException {
		String line = textFileReader.readLine();
		int numLines = Integer.valueOf(line);
		StringBuilder textBuilder = new StringBuilder();
		for (int i = 0; i < numLines; ++i) {
			textBuilder.append(textFileReader.readLine());
			textBuilder.append("\n");
		}

		String text = textBuilder.toString();
		List<List<TaggedWord>> taggedSentences = tagText(text, tagger);

		dstFileWriter.write(String.format("%d\n", taggedSentences.size()));

		for (List<TaggedWord> sentence : taggedSentences) {
			for (TaggedWord tw : sentence) {
				dstFileWriter.write(String.format("%s\t%s\t%s\n", text.substring(tw.beginPosition(), tw.endPosition()),
						tw.value(), tw.tag()));
			}
			dstFileWriter.write("\n");
		}
	}

	public static void tagTextFileDocBlocks() throws IOException {
		boolean splitSentence = true;

		String dataset = "LDC2015E75";
//		String dataset = "LDC2015E103";
//		String dataset = "LDC2016E63";

		String textFile = String.format("/home/dhl/data/EDL/%s/data/doc-text-clean.txt", dataset);
		String outputFile = String.format("/home/dhl/data/EDL/%s/data/doc-text-words-sen.txt", dataset);

//		String modelPath = "edu/stanford/nlp/models/srparser/englishSR.ser.gz";
		String taggerPath = "/media/dhl/Data/lib/stanford-nlp/stanford-postagger-2015-12-09/" +
				"models/english-left3words-distsim.tagger";

		MaxentTagger tagger = new MaxentTagger(taggerPath);
//		ShiftReduceParser model = ShiftReduceParser.loadModel(modelPath);

		FileInputStream inStream = new FileInputStream(textFile);
		InputStreamReader isr = new InputStreamReader(inStream, "UTF8");
		BufferedReader textFileReader = new BufferedReader(isr);

		FileOutputStream outStream = new FileOutputStream(outputFile);
		OutputStreamWriter isw = new OutputStreamWriter(outStream, "UTF8");
		BufferedWriter writer = new BufferedWriter(isw);

		int textCnt = 0, docCnt = 0;
		String line = null;
		while ((line = textFileReader.readLine()) != null) {
			// Read docid and number of blocks
			String[] vals = line.split("\t");
			writer.write(line);
			writer.write("\n");
			++docCnt;

			int numBlocks = Integer.valueOf(vals[1]);
			textCnt += numBlocks;
			for (int i = 0; i < numBlocks; ++i) {
				handleNextTextBlock(tagger, textFileReader, writer);
			}

			if (docCnt % 1000 == 0)
				System.out.println(docCnt);
//			break;
		}

		textFileReader.close();
		writer.close();

		System.out.println(String.format("%d docs, %d blocks.", docCnt, textCnt));
	}

	public static void test() {
		String text = "A loophole allowed the 21-year-old to buy the .45-caliber handgun with money given" +
				" to him for his birthday despite a criminal record that included a recent drug possession " +
				"charge, James Comey said.";

		String modelPath = "edu/stanford/nlp/models/srparser/englishSR.ser.gz";
//		String taggerPath = "d:/projects/os/stanford-nlp/stanford-postagger-2015-12-09/models/english-left3words-distsim.tagger";
		String taggerPath = "d:/projects/os/stanford-nlp/stanford-postagger-2015-12-09/models/english-bidirectional-distsim.tagger";

		MaxentTagger tagger = new MaxentTagger(taggerPath);
		ShiftReduceParser model = ShiftReduceParser.loadModel(modelPath);
		List<List<TaggedWord>> taggedSentences = tagText(text, tagger);
		for (List<TaggedWord> sentence : taggedSentences) {
			Tree tree = model.apply(sentence);
			System.out.println(tree.toString());
//			for (TaggedWord tw : sentence) {
//				System.out.println(String.format("%s\t%s", tw.value(), tw.tag()));
//			}
		}
	}

	public static void main(String[] args) throws IOException {
//		test();
//		parseTextFile();
		tagTextFileDocBlocks();
	}
}
