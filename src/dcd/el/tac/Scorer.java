// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.BufferedReader;
import java.io.IOException;

import dcd.el.io.IOUtils;

public class Scorer {
	public static void score(String goldFileName, String sytemResultFileName) {
		BufferedReader goldReader = IOUtils.getUTF8BufReader(goldFileName), sysReader = IOUtils
				.getUTF8BufReader(sytemResultFileName);

		String sysLine = null, goldLine = null;
		int cnt = 0, correctCnt = 0;
		int inKbCnt = 0, inKbCorrectCnt = 0;
		try {
			goldReader.readLine(); // skip first line

			while ((sysLine = sysReader.readLine()) != null) {
				goldLine = goldReader.readLine();

				if (goldLine == null) {
					System.out.println("Query id not consistent!");
					break;
				}

				String[] sysVals = sysLine.split("\t"), goldVals = goldLine
						.split("\t");

				if (!goldVals[0].equals(sysVals[0])) {
					System.out.println("Query id not consistent!");
					break;
				}

				++cnt;
				if (goldVals[1].startsWith("NIL")) {
					if (sysVals[1].equals("NIL"))
						++correctCnt;
				} else {
					++inKbCnt;
					if (sysVals[1].equals(goldVals[1])) {
						++inKbCorrectCnt;
						++correctCnt;
					}
				}
			}

			goldReader.close();
			sysReader.close();

//			System.out.println(cnt + "\t" + inKbCnt);
			System.out.println("accuracy: " + (double) correctCnt / cnt);
			System.out.println("In KB accuracy: " + (double) inKbCorrectCnt
					/ inKbCnt);
			double nilAccuracy = (double) (correctCnt - inKbCorrectCnt) / (cnt - inKbCnt);
			System.out.println("NIL accuracy: " + nilAccuracy);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
