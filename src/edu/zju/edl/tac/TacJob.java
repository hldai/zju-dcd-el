package edu.zju.edl.tac;

import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.obj.Mention;
import edu.zju.edl.feature.LinkingInfoDoc;
import edu.zju.edl.feature.LinkingInfoGen;
import edu.zju.edl.link.LinkingInfoLinker;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;


public class TacJob {
	public static void genLinkingInfo(LinkingInfoGen linkingInfoGen, String mentionsFile,
									  String docPathFile, String outputFile) throws Exception {
		HashMap<String, String> docIdToPath = loadDocPaths(docPathFile);

		Document[] documents = Document.loadEdlFile(mentionsFile);

		System.out.println(String.format("%d docs in doclist file, %d docs in edl file.",
				docIdToPath.size(), documents.length));

		DataOutputStream dos = IOUtils.getBufferedDataOutputStream(outputFile);

		int mentionCnt = 0, docCnt = 0;
		String docPath = null;

		for (Document doc : documents) {
			++docCnt;
			mentionCnt += doc.mentions.length;
			System.out.println("processing " + docCnt + " " + doc.docId + " " + doc.mentions.length);

			docPath = docIdToPath.get(doc.docId);
			doc.loadText(docPath);
			LinkingInfoDoc linkingInfoDoc = linkingInfoGen.getLinkingInfoDoc(doc);
			doc.text = null;
			linkingInfoDoc.toFile(dos);
		}
		dos.close();

		System.out.println(mentionCnt + " mentions. " + docCnt + " documents.");
	}

	// TODO vectrainfile
//	public static void genLinkingFeatures(LinkingBasisGen linkingBasisGen, String mentionsFile,
//										  String docPathFile, String outputFile,
//										  String dstVecTrainFile) throws Exception {
//		HashMap<String, String> docIdToPath = loadDocPaths(docPathFile);
//
//		Document[] documents = Document.loadEdlFile(mentionsFile);
////		if (mentionsFile.endsWith(".xml"))
////			documents = QueryReader.toDocumentsXmlFile(mentionsFile);
////		else
////			documents = QueryReader.toDocumentsEdlFile(mentionsFile);
//
//		DataOutputStream dos = IOUtils.getBufferedDataOutputStream(outputFile);
//
//		DataOutputStream dosTmp = null;
//		if (dstVecTrainFile != null)
//			dosTmp = IOUtils.getBufferedDataOutputStream(dstVecTrainFile);
//
//		int mentionCnt = 0, docCnt = 0;
//		String docPath = null;
//
//		System.out.println(documents.length + " docs.");
//		if (dosTmp != null)
//			dosTmp.writeInt(documents.length);
//
//		for (Document doc : documents) {
//			++docCnt;
//			mentionCnt += doc.mentions.length;
//			System.out.println("processing " + docCnt + " " + doc.docId + " " + doc.mentions.length);
//
//			docPath = docIdToPath.get(doc.docId);
//			doc.loadText(docPath);
//			LinkingBasisDoc linkingBasisDoc = linkingBasisGen.getLinkingBasisDoc(doc, 50); // TODO
//			doc.text = null;
//			linkingBasisDoc.toFile(dos);
//
//			if (dosTmp != null)
//				linkingBasisDoc.toFileVecTrain(dosTmp);
////			if (docCnt == 3)
////					break;
//		}
//		dos.close();
//
//		if (dosTmp != null)
//			dosTmp.close();
//
//		System.out.println(mentionCnt + " mentions. " + docCnt + " documents.");
//	}

	public static void linkWithLinkingInfo(LinkingInfoLinker linker, String linkingInfoFile, String mentionsFile,
										   String outputFile) {
		System.out.println(linkingInfoFile);
		System.out.println(mentionsFile);
		System.out.println(outputFile);

		boolean useMid = true; // TODO

		DataInputStream dis = IOUtils.getBufferedDataInputStream(linkingInfoFile);
		LinkingInfoDoc linkingInfoDoc = new LinkingInfoDoc();
		HashMap<String, String> mentionIdToKbid = new HashMap<>();
		while (linkingInfoDoc.fromFile(dis)) {
			LinkingResult[] results;
			if (useMid) {
				results = linker.link(linkingInfoDoc);
			} else {
				results = linker.link14(linkingInfoDoc);
			}

			for (LinkingResult result : results) {
				mentionIdToKbid.put(result.queryId, result.kbid);
			}
		}

//		CrossDocNilHandler.handle(mentionIdToKbid, mentionsFile);
		saveLinkingResults(mentionIdToKbid, mentionsFile, outputFile);
	}

	private static void saveLinkingResults(HashMap<String, String> el, String edlFile, String dstFile) {
		BufferedWriter writer = IOUtils.getUTF8BufWriter(dstFile, false);
		LinkedList<Mention> mentions = Mention.loadEdlFile(edlFile);
		try {
			for (Mention m : mentions) {
				String kbid = el.getOrDefault(m.mentionId, "NIL000001");
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

	private static HashMap<String, String> loadDocPaths(String docPathFile) throws IOException {
		BufferedReader reader = IOUtils.getUTF8BufReader(docPathFile);
		HashMap<String, String> docIdToPath = new HashMap<>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			// get doc ID
			int begPosLinux = line.lastIndexOf('/');
			int begPosWin = line.lastIndexOf('\\');
			int begPos = begPosLinux > begPosWin ? begPosLinux + 1 : begPosWin + 1;
			int endPos = 0;
			if (line.endsWith(".df.xml") || line.endsWith(".nw.xml"))
				endPos = line.length() - 7;
			else if (line.endsWith(".xml"))
				endPos = line.length() - 4;
			else
				assert false;
			String docId = line.substring(begPos, endPos);

			docIdToPath.put(docId, line);
//			System.out.println(String.format("%s -> %s", docId, line));
		}
		reader.close();

		return docIdToPath;
	}
}
