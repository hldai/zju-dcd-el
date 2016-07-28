package edu.zju.dcd.edl.tac;

import edu.zju.dcd.edl.config.IniFile;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.linker.SimpleLinker;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.obj.Mention;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by dhl on 16-7-28.
 */
public class TacJob {
	public static void linkWithFeatures(SimpleLinker simpleLinker, String featureFile, String mentionsFile,
										String outputFile) {
		System.out.println(featureFile);
		System.out.println(mentionsFile);
		System.out.println(outputFile);

		boolean useMid = true; // TODO

		DataInputStream dis = IOUtils.getBufferedDataInputStream(featureFile);
		LinkingBasisDoc linkingBasisDoc = new LinkingBasisDoc();
		HashMap<String, String> mentionIdToKbid = new HashMap<String, String>();
		while (linkingBasisDoc.fromFile(dis)) {
			LinkingResult[] results;
			if (useMid) {
				results = simpleLinker.link(linkingBasisDoc);
			} else {
				results = simpleLinker.link14(linkingBasisDoc);
			}

			for (LinkingResult result : results) {
				// writer.write(result.queryId + "\t" + result.kbid + "\n");
//				resultList.add(result);
				mentionIdToKbid.put(result.queryId, result.kbid);
			}
		}

//		Collections.sort(resultList, new LinkingResult.ComparatorOnQueryId());
//		CrossDocNilHandler.handle(resultList, queryFileName);
		saveLinkingResults(mentionIdToKbid, mentionsFile, outputFile);
	}

	private static void saveLinkingResults(HashMap<String, String> el, String edlFile, String dstFile) {
		BufferedWriter writer = IOUtils.getUTF8BufWriter(dstFile, false);
		LinkedList<Mention> mentions = Mention.loadEdlFile(edlFile);
		try {
			for (Mention m : mentions) {
				String kbid = el.getOrDefault(m.mentionId, "NIL0001");
				if (!kbid.startsWith("NIL"))
					kbid = "m." + kbid;
//				System.out.println(String.format("%s\t%s", m.mentionId, kbid));
				writer.write(String.format("%s\t%s\t%s\t%s:%d-%d\t%s\t%s\t%s\t1.0\n", "ZJU", m.mentionId,
						m.nameString, m.docId, m.beg, m.end, kbid, m.entityType, m.mentionType));
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
