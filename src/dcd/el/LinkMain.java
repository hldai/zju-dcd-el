// author: DHL brnpoem@gmail.com

package dcd.el;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import dcd.config.IniFile;
import dcd.el.io.IOUtils;
import dcd.el.linker.NaiveLinker;
import dcd.el.objects.Mention;
import dcd.el.dict.AliasDictWithIndex;
import dcd.el.feature.FeatureLoader;
import dcd.el.tac.CandidateFeatureGen;
import dcd.el.tac.LinkingJob;
import dcd.el.tac.MidToEidMapper;

public class LinkMain {
	
	public static void test() {
	}
	
	public static void linkTestWithQueryFile(IniFile config) {
		LinkingJob.run(config);
	}
	
	public static void runByConfig(IniFile config) {
		IniFile.Section mainSect = config.getSection("main");
		String job = mainSect.getValue("job");
		System.out.println("job: " + job);
		if (job.startsWith("link")) {
			linkTestWithQueryFile(config);
		} else if (job.startsWith("gen_local_feature")) {
			CandidateFeatureGen.run(config);
		} else if (job.equals("test")) {
			test();
		}
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		IniFile config = new IniFile("d:/data/el/config/tac_link.ini");
		runByConfig(config);

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
