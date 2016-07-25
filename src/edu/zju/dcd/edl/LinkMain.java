// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl;

import edu.zju.dcd.edl.config.IniFile;
import edu.zju.dcd.edl.tac.CandidatesRetrieverStat;
import edu.zju.dcd.edl.tac.LinkingJob;
import edu.zju.dcd.edl.tac.Query;
import edu.zju.dcd.edl.tac.QueryList;

public class LinkMain {
	
	public static void test() {
		String queryDir=  "d:/data/el/LDC2015E20_EDL_2014/data/eval";
		QueryList queryList = new QueryList(queryDir);
		Query q = queryList.getQuery("EDL14_ENG_0019");
		System.out.println(q.name + "\t" + q.type);
		q = queryList.getQuery("EDL14_ENG_0020");
		System.out.println(q.name + "\t" + q.type);
	}

	public static void runByConfig(IniFile config) {
		IniFile.Section mainSect = config.getSection("main");
		String job = mainSect.getValue("job");
		System.out.println("job: " + job);
		if (job.startsWith("link") || job.startsWith("gen_linking_basis")) {
			LinkingJob.run(config);
		} else if (job.equals("test")) {
			test();
		} else if (job.equals("candidate_retrieve_stat"))
			CandidatesRetrieverStat.genStat(config);
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		String dataDir = "/home/dhl/data/EDL/";
		String configFile = dataDir + "config/tac-link.ini";

		IniFile config = new IniFile(configFile);
		runByConfig(config);
//		test();

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
