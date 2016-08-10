// author: DHL brnpoem@gmail.com

package edu.zju.edl;

import edu.zju.edl.link.DCDLinker;
import edu.zju.edl.tac.MidToEidMapper;
import edu.zju.edl.tac.TacJob;
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
		options.addOption("info", true, "linking info file");
		options.addOption("mentions", true, "mentions file");
		options.addOption("o", true, "output file");

		CommandLineParser cmParser = new DefaultParser();
		CommandLine cmd = cmParser.parse(options, args);
		linking(cmd);
	}

	private static void linking(CommandLine cmd) {
		System.out.println("linking ...");
//		SimpleNaiveLinker linker = getLinker(cmd);
		DCDLinker linker = getDCDLinker(cmd);
		String linkingInfoFile = cmd.getOptionValue("info");
		String mentionsFile = cmd.getOptionValue("mentions");
		String outputFile = cmd.getOptionValue("o");
		TacJob.linkWithLinkingInfo(linker, linkingInfoFile, mentionsFile, outputFile);
	}

	private static DCDLinker getDCDLinker(CommandLine cmd) {
		boolean useMid = cmd.hasOption("f");

		String resourceDir = cmd.getOptionValue("res");

		MidToEidMapper mteMapper = null;
		if (!useMid) {
			mteMapper = new MidToEidMapper(Paths.get(resourceDir, "prog-gen/mid-to-eid.bin").toString());
		}

		return new DCDLinker(mteMapper);
	}

//	private static SimpleNaiveLinker getLinker(CommandLine cmd) {
//		boolean useMid = cmd.hasOption("f");
//
//		String resourceDir = cmd.getOptionValue("res");
//
//		MidToEidMapper mteMapper = null;
//		if (!useMid) {
//			mteMapper = new MidToEidMapper(Paths.get(resourceDir, "prog-gen/mid-to-eid.bin").toString());
//		}
//
//		String filterMidsFile = Paths.get(resourceDir, "freebase/filter_mids_10_8.bin").toString();
//		MidFilter midFilter = null;
////		MidFilter midFilter = new MidFilter(filterMidsFile);
//
//		return new SimpleNaiveLinker(mteMapper, midFilter, null);
//	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

//		runByConfig();
		runByArgs(args);

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
