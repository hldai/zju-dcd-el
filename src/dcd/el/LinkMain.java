// author: DHL brnpoem@gmail.com

package dcd.el;

import dcd.config.IniFile;
import dcd.el.tac.CandidateFeatureGen;
import dcd.el.tac.CandidatesRetrieverStat;
import dcd.el.tac.LinkingJob;
import dcd.el.utils.CommonUtils;

public class LinkMain {
	
	public static void test() {
		String text = "bob dylan's", word = "dylan";
		if (CommonUtils.hasWord(text, word))
			System.out.println("hit");
	}
		
	public static void runByConfig(IniFile config) {
		IniFile.Section mainSect = config.getSection("main");
		String job = mainSect.getValue("job");
		System.out.println("job: " + job);
		if (job.startsWith("link")) {
			LinkingJob.run(config);
		} else if (job.startsWith("gen_local_feature")) {
			CandidateFeatureGen.run(config);
		} else if (job.equals("test")) {
			test();
		} else if (job.equals("candidate_retrieve_stat"))
			CandidatesRetrieverStat.genStat(config);
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		IniFile config = new IniFile("d:/data/el/config/tac_link.ini");
		runByConfig(config);
//		test();

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
