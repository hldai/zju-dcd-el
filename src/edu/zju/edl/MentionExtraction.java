package edu.zju.edl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

public class MentionExtraction {
	public static void extractTest(String serializedClassifier) {
		AbstractSequenceClassifier<CoreLabel> classifier = null;
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String text = "University of Westminster";

		List<Triple<String, Integer, Integer>> triples = classifier
				.classifyToCharacterOffsets(text);
		System.out.println(triples.size());
		for (Triple<String, Integer, Integer> trip : triples) {
			System.out.println(String.format("%s\t%d\t%d\t%s", text.substring(trip.second, trip.third),
					trip.second, trip.third, trip.first));
		}
	}

	private static void writeNerResult(String text, List<List<CoreLabel>> nerOutput, BufferedWriter writer) throws IOException {
		int wordCnt = 0;
		for (List<CoreLabel> sentence : nerOutput)
			wordCnt += sentence.size();

		writer.write(wordCnt + "\n");
		for (List<CoreLabel> sentence : nerOutput) {
			for (CoreLabel word : sentence) {
//				writer.write(String.format("%s\t%s\n", word.word(),
//						word.get(CoreAnnotations.AnswerAnnotation.class)));
				writer.write(String.format("%s\t%s\n", text.substring(word.beginPosition(), word.endPosition()),
						word.get(CoreAnnotations.AnswerAnnotation.class)));
			}
		}
	}

	public static void extract(String serializedClassifier, String tacTextFile, String dstFile0,
							   String dstFile1) throws IOException {
		AbstractSequenceClassifier<CoreLabel> classifier = null;
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier);
		} catch (Exception e) {
			e.printStackTrace();
		}

		FileInputStream inStream = new FileInputStream(tacTextFile);
		BufferedReader textFileReader = new BufferedReader(new InputStreamReader(inStream, "UTF8"));

		FileOutputStream outStream0 = new FileOutputStream(dstFile0);
		BufferedWriter writer0 = new BufferedWriter(new OutputStreamWriter(outStream0, "UTF8"));

		FileOutputStream outStream1 = new FileOutputStream(dstFile1);
		BufferedWriter writer1 = new BufferedWriter(new OutputStreamWriter(outStream1, "UTF8"));

		int cnt = 0;
		String line = null;
		while ((line = textFileReader.readLine()) != null) {
			String[] vals = line.split("\t");
			int numLines = Integer.valueOf(vals[0]);
			StringBuilder textBuilder = new StringBuilder();
			for (int i = 0; i < numLines; ++i) {
				textBuilder.append(textFileReader.readLine());
				textBuilder.append("\n");
			}

			String text = textBuilder.toString();
			text = text.replaceAll("al-", "Al-");

			List<List<CoreLabel>> out = classifier.classify(text);
			writeNerResult(text, out, writer0);

			text = text.replaceAll("https?:\\S*", " ");
			text = text.replaceAll("[/-]", " ");
			out = classifier.classify(text);
			writeNerResult(text, out, writer1);

			++cnt;
			if (cnt % 1000 == 0)
				System.out.println(cnt);

//			break;
		}

		textFileReader.close();
		writer0.close();
		writer1.close();
	}

	private static String readLines(BufferedReader reader, int numLines) throws IOException {
		StringBuilder textBuilder = new StringBuilder();
		for (int i = 0; i < numLines; ++i) {
			textBuilder.append(reader.readLine());
			textBuilder.append("\n");
		}

		return textBuilder.toString();
	}

	private static void extractTB(String serializedClassifier, String tacTextFile, String dstFile0,
								 String dstFile1) throws IOException {
		AbstractSequenceClassifier<CoreLabel> classifier = null;
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier);
		} catch (Exception e) {
			e.printStackTrace();
		}

		FileInputStream inStream = new FileInputStream(tacTextFile);
		BufferedReader textFileReader = new BufferedReader(new InputStreamReader(inStream, "UTF8"));

		FileOutputStream outStream0 = new FileOutputStream(dstFile0);
		BufferedWriter writer0 = new BufferedWriter(new OutputStreamWriter(outStream0, "UTF8"));

		FileOutputStream outStream1 = new FileOutputStream(dstFile1);
		BufferedWriter writer1 = new BufferedWriter(new OutputStreamWriter(outStream1, "UTF8"));

		int cnt = 0;
		String line = null;
		while ((line = textFileReader.readLine()) != null) {
			String[] vals = line.split("\t");
			String docid = vals[0];
			int numBlocks = Integer.valueOf(vals[1]);

			for (int i = 0; i < numBlocks; ++i) {
				int numLines = Integer.valueOf(textFileReader.readLine());
				String text = readLines(textFileReader, numLines);
				text = text.replaceAll("al-", "Al-");

				List<List<CoreLabel>> out = classifier.classify(text);
				writeNerResult(text, out, writer0);

				text = text.replaceAll("https?:\\S*", " ");
				text = text.replaceAll("[/-]", " ");
				out = classifier.classify(text);
				writeNerResult(text, out, writer1);
			}

			++cnt;
			if (cnt % 1000 == 0)
				System.out.println(cnt);

//			break;
		}

		textFileReader.close();
		writer0.close();
		writer1.close();
	}

	public static void main(String[] args) throws IOException {
//		String dataset = "LDC2015E75";
		String dataset = "LDC2015E103";
//		String dataset = "LDC2016E63";

//		String serializedClassifier = "/media/dhl/Data/lib/stanford-nlp/stanford-ner-2015-12-09" +
//				"/classifiers/english.all.3class.distsim.crf.ser.gz";
		String serializedClassifier = "e:/lib/stanford-nlp/stanford-ner-2015-12-09" +
				"/classifiers/english.all.3class.distsim.crf.ser.gz";

//		String dataDir = "/home/dhl/data/EDL";
		String dataDir = "e:/data/edl";
		String tacTextFile = Paths.get(dataDir, dataset, "data/doc-text-clean-new.txt").toString();
		String dstFile0 = Paths.get(dataDir, dataset, "output/ner-result0.txt").toString();
		String dstFile1 = Paths.get(dataDir, dataset, "output/ner-result1.txt").toString();

		extractTB(serializedClassifier, tacTextFile, dstFile0, dstFile1);
//		extractTest(serializedClassifier);
	}
}