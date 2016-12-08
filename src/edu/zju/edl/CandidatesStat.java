package edu.zju.edl;

import edu.zju.edl.cg.CandidatesDict;
import edu.zju.edl.cg.CandidatesGen;
import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.obj.Document;
import edu.zju.edl.obj.Mention;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by dhl on 16-7-28.
 * Test the performance of candidates generation
 */
public class CandidatesStat {
	private static void runByArgs(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("res", true, "resource directory");
		options.addOption("gold", true, "gold edl file");
		options.addOption("o", true, "output file");

		CommandLineParser cmParser = new DefaultParser();
		CommandLine cmd = cmParser.parse(options, args);
		stat(cmd);
	}

	private static void stat(CommandLine cmd) throws IOException {
		String goldEdlFile = cmd.getOptionValue("gold");
		String errorFileName = cmd.getOptionValue("o");

		String resourceDir = cmd.getOptionValue("res");

		CandidatesGen candidatesGen = getCandidatesGen(resourceDir);
		genStatCG(candidatesGen, goldEdlFile, errorFileName);

//		CandidatesRetriever candidatesRetriever = getCandidatesRetriever(resourceDir);
//		genStat(candidatesRetriever, null, goldEdlFile, errorFileName);
	}

	private static CandidatesGen getCandidatesGen(String resourceDir) {
		String candidatesDictFile = Paths.get(resourceDir, "prog-gen/candidates-dict.bin").toString();
		CandidatesDict candidatesDict = new CandidatesDict(candidatesDictFile);

		String personListFile = Paths.get(resourceDir, "freebase/person_list.txt").toString();
		String nameDictFile = Paths.get(resourceDir, "names-dict.txt").toString();
		return new CandidatesGen(candidatesDict, personListFile, nameDictFile);
	}

	private static void genStatCG(CandidatesGen candidatesGen, String goldEdlFile,
								 String errorFileName) throws IOException {
		BufferedWriter writer = IOUtils.getUTF8BufWriter(errorFileName, false);
		Document[] documents = Document.loadEdlFile(goldEdlFile, true);

		int queryCnt = 0, hitCnt = 0, inKbCnt = 0, inKbHitCnt = 0;
		int candidateCnt = 0;
		for (Document doc : documents) {
			System.out.println(doc.docId);
			queryCnt += doc.mentions.length;

			Mention[] curMentions = doc.mentions;
			int[] corefChain = new int[doc.mentions.length];
			CandidatesDict.CandidatesEntry[] candidatesEntries = candidatesGen.getCandidatesOfMentionsInDoc(doc,
					corefChain);  // TODO
			for (int i = 0; i < candidatesEntries.length; ++i) {
				CandidatesDict.CandidatesEntry curCandidatesEntry = candidatesEntries[i];

				boolean hitFlg = false;

				if (curMentions[i].kbid.startsWith("NIL")) {
					++hitCnt;
					hitFlg = true;
				} else {
					++inKbCnt;

					if (candidatesEntries[i] != null) {
						for (int j = 0; j < curCandidatesEntry.mids.length; ++j) {
							if (curCandidatesEntry.mids[j].toString().equals(curMentions[i].kbid.substring(2))) {
								++hitCnt;
								++inKbHitCnt;
								hitFlg = true;
								break;
							}
						}
//						System.out.println(curMentions[i].kbid + "\t" + candidatesEntries[i].mids[0].toString());
					}
				}
//
				if (!hitFlg) {
					writer.write(String.format("%s\t%s\t%s\t%s\n", doc.docId, curMentions[i].nameString,
							curMentions[i].mentionId, curMentions[i].kbid));
				}
			}
		}

		writer.close();

		System.out.println(queryCnt + " queries. " + inKbCnt
				+ " queries in KB.");
		System.out.println("Average number of candidates: " + (double)candidateCnt / queryCnt);
		System.out.println("Recall: " + (double) hitCnt / queryCnt);
		System.out.println("In KB recall: " + (double) inKbHitCnt / inKbCnt);
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

//		runByConfig();
		runByArgs(args);

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
