package edu.zju.dcd.edl;

import edu.zju.dcd.edl.cg.CandidatesDict;
import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.cg.IndexedAliasDictWithPse;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.tac.MidToEidMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by dhl on 16-7-28.
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

	public static void stat(CommandLine cmd) throws IOException {
		String resourceDir = cmd.getOptionValue("res");

		String candidatesDictFile = Paths.get(resourceDir, "prog-gen/candidates-dict.bin").toString();
		CandidatesDict candidatesDict = new CandidatesDict(candidatesDictFile);

//		CandidatesRetriever candidatesRetriever = getCandidatesRetriever(resourceDir);
//
//		String goldEdlFile = cmd.getOptionValue("gold");
//		String errorFileName = cmd.getOptionValue("o");
//
//		genStat(candidatesRetriever, null, goldEdlFile, errorFileName);
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
		String gpeAdjListFile = Paths.get(resourceDir, "nation_adj.txt").toString();
		return new CandidatesRetriever(indexedAliasDictWithPse, midPopularityFile,
				personListFile, gpeAdjListFile, mteMapper);
	}

	public static void genStat(CandidatesRetriever candidatesRetriever, MidToEidMapper mapper,
							   String goldEdlFile, String errorFileName) throws IOException {
		LinkingResult.ComparatorOnQueryId cmpOnQueryId = new LinkingResult.ComparatorOnQueryId();
		LinkingResult[] goldResults = LinkingResult.getGroudTruthTab(goldEdlFile);
		Arrays.sort(goldResults, cmpOnQueryId);

		BufferedWriter writer = IOUtils.getUTF8BufWriter(errorFileName, false);
		Document[] documents = Document.loadEdlFile(goldEdlFile);
		int queryCnt = 0, hitCnt = 0, inKbCnt = 0, inKbHitCnt = 0;
		int candidateCnt = 0;
		LinkingResult tmpResult = new LinkingResult();
		for (Document doc : documents) {
			System.out.println(doc.docId);
			queryCnt += doc.mentions.length;

			CandidatesRetriever.CandidatesOfMention[] candidatesOfMentions = candidatesRetriever
					.getCandidatesInDocument(doc);
			for (int i = 0; i < candidatesOfMentions.length; ++i) {
				tmpResult.queryId = doc.mentions[i].mentionId;
				if (candidatesOfMentions[i].candidates != null) {
					candidateCnt += candidatesOfMentions[i].candidates.length;
				}

				int pos = Arrays.binarySearch(goldResults, tmpResult,
						cmpOnQueryId);
				if (pos < 0) {
					System.err
							.println("error: query id not found in ground truth. "
									+ tmpResult.queryId);
					break;
				}

				boolean hitFlg = false;
				String goldKbId = goldResults[pos].kbid;
				if (goldKbId.startsWith(ELConsts.NIL)) {
					++hitCnt;
					hitFlg = true;
				} else {
					++inKbCnt;

					hitFlg = candidateHit(goldKbId, candidatesOfMentions[i].candidates, mapper);
					if (hitFlg) {
						++hitCnt;
						++inKbHitCnt;
					}
				}

				if (!hitFlg) {
					writer.write(String.format("%s\t%s\t%s\t%s\n", doc.docId, doc.mentions[i].nameString,
							goldResults[pos].queryId, goldResults[pos].kbid));
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

	private static boolean candidateHit(String goldKbId, CandidatesRetriever.CandidateWithPopularity[] candidates,
										MidToEidMapper mapper) {
		if (candidates == null)
			return false;

		if (goldKbId.startsWith("m."))
			goldKbId = goldKbId.substring(2);

		int candCnt = 0;
		for (CandidatesRetriever.CandidateWithPopularity candidate : candidates) {
			String sysKbid;
			if (mapper == null)
				sysKbid = candidate.mid.toString().trim();
			else
				sysKbid = mapper.getEid(candidate.mid);

			if (sysKbid != null && sysKbid.equals(goldKbId))
				return true;

			if (++candCnt == 50)
				return false;
		}
		return false;
	}

	private static class TC {
		public int val = 3;
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

//		runByConfig();
		runByArgs(args);

		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 1000.0 + " seconds used.");
	}
}
