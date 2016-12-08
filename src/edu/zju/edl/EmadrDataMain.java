package edu.zju.edl;

import edu.zju.edl.feature.LinkingInfoDoc;
import edu.zju.edl.feature.LinkingInfoMention;
import edu.zju.edl.obj.LinkingResult;
import edu.zju.edl.tac.MidToEidMapper;
import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.utils.WidMidMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by dhl on 11/21/2016.
 */
public class EmadrDataMain {
	private static String[] loadDocListFile(String docListFile) {
		BufferedReader reader = IOUtils.getUTF8BufReader(docListFile);
		LinkedList<String> docPaths = new LinkedList<>();
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				docPaths.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return docPaths.toArray(new String[docPaths.size()]);
	}

	private static Map<String, Integer> mapDocIdToIndex(String[] docPaths) {
		HashMap<String, Integer> mm = new HashMap<>();
		for (int i = 0; i < docPaths.length; ++i) {
			String curpath = docPaths[i];
			int tmp = curpath.lastIndexOf('\\');
			int pos = curpath.lastIndexOf('/');
			pos = pos > tmp ? pos : tmp;
			String docId = null;
			if (curpath.endsWith(".xml"))
				docId = curpath.substring(pos + 1, curpath.length() - 4);
			if (curpath.endsWith(".df.xml") || curpath.endsWith(".nw.xml") )
				docId = curpath.substring(pos + 1, curpath.length() - 7);
			mm.put(docId, i);
		}
		return mm;
	}

	private static int[] loadWikiIds(String wikiIdFile) {
		if (wikiIdFile == null)
			return null;

		int[] wids = null;
		try {
			FileInputStream fs = new FileInputStream(wikiIdFile);
			FileChannel fc = fs.getChannel();
			int numWids = IOUtils.readLittleEndianInt(fc);
			System.out.println(numWids + " wids");

			wids = new int[numWids];

			ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES * numWids);
			buf.order(ByteOrder.LITTLE_ENDIAN);
			fc.read(buf);
			buf.rewind();

			buf.asIntBuffer().get(wids);

			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wids;
	}

	private static float[] getWikiVec(String mid, float[][] wikiVecs, int[] wids, WidMidMapper midWidMapper) {
		if (wikiVecs == null)
			return null;

		int wid = midWidMapper.getWid(mid);
		if (wid < 0)
			return null;

		int pos = Arrays.binarySearch(wids, wid);
		if (pos < 0)
			return null;
		return wikiVecs[pos];
	}

	private static LinkedList<LinkingInfoDoc> loadLinkingInfo(String linkingInfoFile) {
		LinkedList<LinkingInfoDoc> linkingInfoDocs = new LinkedList<>();
		DataInputStream dis = IOUtils.getBufferedDataInputStream(linkingInfoFile);
		try {
			LinkingInfoDoc linkingInfoDoc = new LinkingInfoDoc();
			while (linkingInfoDoc.fromFile(dis)) {
				linkingInfoDocs.add(linkingInfoDoc);
				linkingInfoDoc = new LinkingInfoDoc();
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return linkingInfoDocs;
	}

	private static void prepareEmadrData(String linkingInfoFile, String docListFile, String docVecsFile,
										 String wikiVecsFile, String midToEidFile, String midToWidFile,
										 String wikiIdFile, String dstFile) {
		String[] docPaths = loadDocListFile(docListFile);
		Map<String, Integer> docIdToIndex = mapDocIdToIndex(docPaths);
		float[][] wikiVecs = IOUtils.loadVectors(wikiVecsFile);
		float[][] docVecs = IOUtils.loadVectors(docVecsFile);
		MidToEidMapper midToEidMapper = new MidToEidMapper(midToEidFile);
		WidMidMapper midWidMapper = new WidMidMapper(midToWidFile);
		int[] wids = loadWikiIds(wikiIdFile);
		LinkedList<LinkingInfoDoc> linkingInfoDocs = loadLinkingInfo(linkingInfoFile);

		for (int i = 0; i < 5; ++i) {
			for (float f : docVecs[i])
				System.out.print(String.format("%f ", f));
			System.out.println();
		}

		DataOutputStream dos = IOUtils.getBufferedDataOutputStream(dstFile);
		try {
			dos.writeInt(linkingInfoDocs.size());
			dos.writeInt(docVecs[0].length);
			for (LinkingInfoDoc linkingInfoDoc : linkingInfoDocs) {
				int docIdx = docIdToIndex.get(linkingInfoDoc.docId);
				linkingInfoDoc.docVec = docVecs[docIdx];
				System.out.println(String.format("%s %d", linkingInfoDoc.docId,
						linkingInfoDoc.linkingInfoMentions.length));

				for (int i = 0; i < linkingInfoDoc.linkingInfoMentions.length; ++i) {
					LinkingInfoMention linkingInfoMention = linkingInfoDoc.linkingInfoMentions[i];
					int nb = linkingInfoMention.mids.length;
//					System.out.println(String.format("b %d", linkingInfoMention.mids.length));
					if (linkingInfoDoc.corefChain[i] > -1) {
						linkingInfoMention = linkingInfoDoc.linkingInfoMentions[linkingInfoDoc.corefChain[i]];
//						System.out.println(String.format("a %d", linkingInfoMention.mids.length));
						int na = linkingInfoMention.mids.length;
						if (na < nb)
							System.err.println(String.format("na < nb: %d %d", na, nb));
					}

					linkingInfoMention.wikiVecs = new float[linkingInfoMention.mids.length][];
					for (int j = 0; j < linkingInfoMention.mids.length; ++j) {
						linkingInfoMention.wikiVecs[j] = getWikiVec(linkingInfoMention.mids[j].toString().trim(),
								wikiVecs, wids, midWidMapper);
					}
				}
//				for (LinkingInfoMention linkingInfoMention : linkingInfoDoc.linkingInfoMentions) {
//					System.out.println(String.format("%d mids", linkingInfoMention.mids.length));
//				}
				linkingInfoDoc.toFileVecTrain(dos, midToEidMapper);
			}
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runByArgs(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("info", true, "linking info file");
		options.addOption("docvecs", true, "document vectors file");
		options.addOption("doclist", true, "document list file");
		options.addOption("wikivecs", true, "wiki vectors file");
		options.addOption("midtoeid", true, "mid to eid file");
		options.addOption("midtowid", true, "mid to wid file");
		options.addOption("wid", true, "wid file");
		options.addOption("d", true, "dst file");

//		options.addOption("mentions", true, "mentions file");
//		options.addOption("dl", true, "document path list file");
//		options.addOption("o", true, "output file");

		CommandLineParser cmParser = new DefaultParser();
		CommandLine cmd = cmParser.parse(options, args);
		prepareEmadrDataByArgs(cmd);
	}

	private static void prepareEmadrDataByArgs(CommandLine cmd) throws Exception {
		String linkingInfoFile = cmd.getOptionValue("info");
		String docListFile = cmd.getOptionValue("doclist");
		String docVecsFile = cmd.getOptionValue("docvecs");
		String wikiVecsFile = cmd.getOptionValue("wikivecs");
		String midToWidFile = cmd.getOptionValue("midtowid");
		String midToEidFile = cmd.getOptionValue("midtoeid", null);
		String wikiIdFile = cmd.getOptionValue("wid");
		String dstFile = cmd.getOptionValue("d");

		System.out.println(String.format("linking info file: %s", linkingInfoFile));
		System.out.println(String.format("doc list file: %s", docListFile));
		System.out.println(String.format("doc vecs file: %s", docVecsFile));
		System.out.println(String.format("wiki vecs file: %s", wikiVecsFile));
		System.out.println(String.format("mid to wid file: %s", midToWidFile));
		System.out.println(String.format("wid file: %s", wikiIdFile));
		System.out.println(String.format("dst file: %s", dstFile));
		prepareEmadrData(linkingInfoFile, docListFile, docVecsFile, wikiVecsFile, midToEidFile, midToWidFile,
				wikiIdFile, dstFile);
	}

	public static void main(String[] args) throws Exception {
		runByArgs(args);
	}
}
