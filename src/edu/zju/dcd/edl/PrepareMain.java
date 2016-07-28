package edu.zju.dcd.edl;

import edu.zju.dcd.edl.feature.FeatureLoader;
import edu.zju.dcd.edl.feature.TfIdfExtractor;
import edu.zju.dcd.edl.tac.MidToEidMapper;
import edu.zju.dcd.edl.utils.WidMidMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.nio.file.Paths;

/**
 * Created by dhl on 16-7-28.
 */
public class PrepareMain {
	private static void runByArgs(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("res", true, "resource directory");
		options.addOption("mentions", true, "mentions file");
		options.addOption("dd", true, "directory of documents");
		options.addOption("o", true, "output file");

		CommandLineParser cmParser = new DefaultParser();
		CommandLine cmd = cmParser.parse(options, args);
		prepare(cmd);
	}

	private static void prepare(CommandLine cmd) {
		String resourceDir = cmd.getOptionValue("res");

		String idfFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf.bin").toString();
		String tfidfFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf.bin").toString();
		String tfidfIdxFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf-index.bin").toString();

		String wikiVecsFile = null, widListFile = null;

		FeatureLoader featureLoader = new FeatureLoader(tfidfFile, tfidfIdxFile);
		TfIdfExtractor tfIdfExtractor = new TfIdfExtractor(idfFile);
		// TODO set as null
		WidMidMapper midWidMapper;
		MidToEidMapper mteMapper;
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

//		runByConfig();
		runByArgs(args);

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
