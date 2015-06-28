// author: DHL brnpoem@gmail.com

package dcd.el;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.TreeMap;

import dcd.config.IniFile;
import dcd.el.feature.BagOfWords;
import dcd.el.io.IOUtils;
import dcd.el.tac.CandidatesRetrieverStat;
import dcd.el.tac.LinkingJob;

public class LinkMain {
	
	public static void test() {
		BufferedReader reader = IOUtils.getUTF8BufReader("d:/data/el/LDC2015E20_EDL_2014/data/training/source_documents/eng-NG-31-128394-9266316.xml");
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String text = new String(sb);
		TreeMap<String, Integer> wordCnts = BagOfWords.toBagOfWords(text);
		System.out.println(wordCnts.get("coordinator"));
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

		IniFile config = new IniFile("d:/data/el/config/tac_link.ini");
		runByConfig(config);
//		test();

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
