package edu.zju.edl;

import edu.zju.edl.feature.LinkingInfoDoc;
import edu.zju.edl.obj.LinkingResult;
import edu.zju.edl.utils.IOUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.DataInputStream;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by dhl on 11/21/2016.
 */
public class EmadrDataMain {
	private static void runByArgs(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("info", true, "linking info file");
		options.addOption("mentions", true, "mentions file");
		options.addOption("dl", true, "document path list file");
		options.addOption("o", true, "output file");

		CommandLineParser cmParser = new DefaultParser();
		CommandLine cmd = cmParser.parse(options, args);
		prepareEmadrData(cmd);
	}

	private static void prepareEmadrData(CommandLine cmd) throws Exception {
		String linkingInfoFile = cmd.getOptionValue("info");
		System.out.println(linkingInfoFile);
		DataInputStream dis = IOUtils.getBufferedDataInputStream(linkingInfoFile);
		LinkingInfoDoc linkingInfoDoc = new LinkingInfoDoc();
		while (linkingInfoDoc.fromFile(dis)) {
			System.out.println(linkingInfoDoc.docId);
		}
	}

	public static void main(String[] args) throws Exception {
		runByArgs(args);
	}
}
