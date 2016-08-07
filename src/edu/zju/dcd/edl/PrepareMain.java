package edu.zju.dcd.edl;

import edu.zju.dcd.edl.cg.CandidatesDict;
import edu.zju.dcd.edl.cg.CandidatesGen;
import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.cg.IndexedAliasDictWithPse;
import edu.zju.dcd.edl.feature.FeatureGen;
import edu.zju.dcd.edl.feature.FeatureLoader;
import edu.zju.dcd.edl.feature.TfIdfExtractor;
import edu.zju.dcd.edl.tac.LinkingBasisGen;
import edu.zju.dcd.edl.tac.MidToEidMapper;
import edu.zju.dcd.edl.tac.TacJob;
import edu.zju.dcd.edl.utils.WidMidMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by dhl on 16-7-28.
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
//		prepare(cmd);
		prepareNew(cmd);
	}

	private static void prepareNew(CommandLine cmd) throws Exception {
		String mentionsFile = cmd.getOptionValue("mentions");
		String docListFile = cmd.getOptionValue("dl");
		String outputFile = cmd.getOptionValue("o");
		String resourceDir = cmd.getOptionValue("res");

		String idfFile = Paths.get(resourceDir, "prog-gen/enwiki-idf.bin").toString();
//		String tfidfFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf.bin").toString();
		String tfidfFile = "c:/data/enwiki-tfidf.bin";
		String tfidfIdxFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf-index.bin").toString();

		FeatureLoader featureLoader = new FeatureLoader(tfidfFile, tfidfIdxFile);
		TfIdfExtractor tfIdfExtractor = new TfIdfExtractor(idfFile);

		CandidatesGen candidatesGen = getCandidatesGen(resourceDir);

		FeatureGen featureGen = new FeatureGen(candidatesGen, featureLoader, tfIdfExtractor);
		TacJob.genLinkingScores(featureGen, mentionsFile, docListFile, outputFile);
	}

	private static void prepare(CommandLine cmd) throws Exception {
		String mentionsFile = cmd.getOptionValue("mentions");
		String docListFile = cmd.getOptionValue("dl");
		String outputFile = cmd.getOptionValue("o");
		String resourceDir = cmd.getOptionValue("res");

		// TODO
		String wikiVecsFile = null, widListFile = null, docVecsFile = null, docIdsFile = null,
				dstVecTrainFile = null;

		String idfFile = Paths.get(resourceDir, "prog-gen/enwiki-idf.bin").toString();
//		String tfidfFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf.bin").toString();
		String tfidfFile = "c:/data/enwiki-tfidf.bin";
		String tfidfIdxFile = Paths.get(resourceDir, "prog-gen/enwiki-tfidf-index.bin").toString();

		FeatureLoader featureLoader = new FeatureLoader(tfidfFile, tfidfIdxFile);
		TfIdfExtractor tfIdfExtractor = new TfIdfExtractor(idfFile);
		// TODO set as null
		WidMidMapper midWidMapper = null;

		CandidatesRetriever candidatesRetriever = getCandidatesRetriever(resourceDir);

		LinkingBasisGen linkingBasisGen = new LinkingBasisGen(candidatesRetriever, featureLoader, tfIdfExtractor,
				midWidMapper, wikiVecsFile, widListFile, docVecsFile, docIdsFile);
		TacJob.genLinkingFeatures(linkingBasisGen, mentionsFile, docListFile, outputFile, dstVecTrainFile);
	}

	private static CandidatesGen getCandidatesGen(String resourceDir) {
		String candidatesDictFile = Paths.get(resourceDir, "prog-gen/candidates-dict.bin").toString();
		CandidatesDict candidatesDict = new CandidatesDict(candidatesDictFile);

		String personListFile = Paths.get(resourceDir, "freebase/person_list.txt").toString();
		String nameDictFile = Paths.get(resourceDir, "names-dict.txt").toString();
		return new CandidatesGen(candidatesDict, personListFile, nameDictFile);
	}

	private static CandidatesRetriever getCandidatesRetriever(String resourceDir) {
		String candidatesFile = Paths.get(resourceDir, "prog-gen/dict_fb_wiki_alias_pse.txt").toString();
		String candidatesIdxFile = Paths.get(resourceDir, "prog-gen/dict_fb_wiki_alias_index_pse.txt").toString();
		String midsFile = Paths.get(resourceDir, "prog-gen/dict_fb_wiki_mid_pse.bin").toString();

		IndexedAliasDictWithPse indexedAliasDictWithPse = new IndexedAliasDictWithPse(candidatesFile,
				candidatesIdxFile, midsFile);
		// TODO set as null
		MidToEidMapper mteMapper = null;

		String midPopularityFile = Paths.get(resourceDir, "prog-gen/mid_pop_link.bin").toString();
		String personListFile = Paths.get(resourceDir, "freebase/person_list.txt").toString();
		String nameDictFile = Paths.get(resourceDir, "names-dict.txt").toString();
		return new CandidatesRetriever(indexedAliasDictWithPse, midPopularityFile,
				personListFile, nameDictFile, mteMapper);
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

		runByArgs(args);

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
