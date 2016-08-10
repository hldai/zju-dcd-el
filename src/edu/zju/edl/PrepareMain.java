package edu.zju.edl;

import edu.zju.dcd.edl.cg.CandidatesDict;
import edu.zju.dcd.edl.cg.CandidatesGen;
import edu.zju.dcd.edl.feature.TfIdfExtractor;
import edu.zju.edl.tac.TacJob;
import edu.zju.edl.feature.FeatureLoader;
import edu.zju.edl.feature.LinkingInfoGen;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.nio.file.Paths;

/**
 * Created by dhl on 16-7-28.
 * Main class for preparation
 */
public class PrepareMain {
	private static void runByArgs(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("res", true, "resource directory");
		options.addOption("mentions", true, "mentions file");
		options.addOption("dl", true, "document path list file");
		options.addOption("o", true, "output file");

		CommandLineParser cmParser = new DefaultParser();
		CommandLine cmd = cmParser.parse(options, args);
		prepare(cmd);
	}

	private static void prepare(CommandLine cmd) throws Exception {
		String mentionsFile = cmd.getOptionValue("mentions");
		String docListFile = cmd.getOptionValue("dl");
		String outputFile = cmd.getOptionValue("o");
		String resourceDir = cmd.getOptionValue("res");

		String idfFile = Paths.get(resourceDir, "prog-gen/enwiki-idf.bin").toString();
		String tfidfFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf.bin").toString();
//		String tfidfFile = "c:/data/enwiki-tfidf.bin";
		String tfidfIdxFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf-index.bin").toString();

		FeatureLoader featureLoader = new FeatureLoader(tfidfFile, tfidfIdxFile);
		TfIdfExtractor tfIdfExtractor = new TfIdfExtractor(idfFile);

		CandidatesGen candidatesGen = getCandidatesGen(resourceDir);

		LinkingInfoGen linkingInfoGen = new LinkingInfoGen(candidatesGen, featureLoader, tfIdfExtractor);
		TacJob.genLinkingInfo(linkingInfoGen, mentionsFile, docListFile, outputFile);

//		FeatureGen featureGen = new FeatureGen(candidatesGen, featureLoader, tfIdfExtractor);
//		TacJob.genLinkingScores(featureGen, mentionsFile, docListFile, outputFile);
	}

	private static CandidatesGen getCandidatesGen(String resourceDir) {
		String candidatesDictFile = Paths.get(resourceDir, "prog-gen/candidates-dict.bin").toString();
		CandidatesDict candidatesDict = new CandidatesDict(candidatesDictFile);

		String personListFile = Paths.get(resourceDir, "freebase/person_list.txt").toString();
		String nameDictFile = Paths.get(resourceDir, "names-dict.txt").toString();
		return new CandidatesGen(candidatesDict, personListFile, nameDictFile);
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

		runByArgs(args);

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
