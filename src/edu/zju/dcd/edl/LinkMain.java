// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl;

import edu.zju.dcd.edl.config.ConfigUtils;
import edu.zju.dcd.edl.config.IniFile;
import edu.zju.dcd.edl.linker.SimpleNaiveLinker;
import edu.zju.dcd.edl.tac.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.nio.file.Paths;

public class LinkMain {
	private static void runByArgs(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("f", false, "use freebase id");

		options.addOption("res", true, "resource directory");
		options.addOption("feat", true, "feature file");
		options.addOption("mentions", true, "mentions file");
		options.addOption("o", true, "output file");

		CommandLineParser cmParser = new DefaultParser();
		CommandLine cmd = cmParser.parse(options, args);
		linking(cmd);
	}

	private static void runByConfig() {
		String dataDir = "/home/dhl/data/EDL/";
		String configFile = dataDir + "config/tac-link.ini";

		IniFile config = new IniFile(configFile);

		IniFile.Section mainSect = config.getSection("main");
		String job = mainSect.getValue("job");
		System.out.println("job: " + job);
		if (job.startsWith("link") || job.startsWith("gen_linking_basis")) {
			LinkingJob.run(config);
		}
	}

	private static void test() {
	}

	private static void linking(CommandLine cmd) {
		System.out.println("linking ...");
		SimpleNaiveLinker linker = getLinker(cmd);
		String featureFile = cmd.getOptionValue("feat");
		String mentionsFile = cmd.getOptionValue("mentions");
		String outputFile = cmd.getOptionValue("o");
		TacJob.linkWithFeatures(linker, featureFile, mentionsFile, outputFile);
	}

	private static SimpleNaiveLinker getLinker(CommandLine cmd) {
		boolean useMid = cmd.hasOption("f");

		String resourceDir = cmd.getOptionValue("res");

		MidToEidMapper mteMapper = null;
		if (!useMid) {
			mteMapper = new MidToEidMapper(Paths.get(resourceDir, "prog-gen/mid-to-eid.bin").toString());
		}

		String filterMidsFile = Paths.get(resourceDir, "freebase/filter_mids_10_8.bin").toString();
		MidFilter midFilter = null;
//		MidFilter midFilter = new MidFilter(filterMidsFile);

		return new SimpleNaiveLinker(mteMapper, midFilter, null);
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

//		runByConfig();
		runByArgs(args);

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
