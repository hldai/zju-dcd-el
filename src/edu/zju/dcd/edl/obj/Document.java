// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.zju.dcd.edl.io.IOUtils;

public class Document {
	private static final String DOC_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";

	public static Document[] loadEdlFile(String edlFile) {
		LinkedList<Mention> mentions = Mention.loadEdlFile(edlFile);
		return mentionsToDocs(mentions);
	}

	public boolean loadText(String srcDocPath) {
		BufferedReader reader = IOUtils.getUTF8BufReader(srcDocPath);
		try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
			
			text = sb.substring(DOC_HEAD.length());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public void releaseText() {
		text = null;
	}

	private static Document[] mentionsToDocs(LinkedList<Mention> mentions) {
		HashMap<String, LinkedList<Mention>> docIdToMentions = arrangeMentionsByDocs(mentions);
		Document[] docs = new Document[docIdToMentions.size()];

		int i = 0;
		for (Map.Entry<String, LinkedList<Mention>> entry : docIdToMentions.entrySet()) {
			docs[i] = new Document();
			docs[i].docId = entry.getKey();
			Mention[] tmpMentions = new Mention[entry.getValue().size()];
			docs[i].mentions = entry.getValue().toArray(tmpMentions);

			Arrays.sort(docs[i].mentions, new Mention.MentionPosComparator());

			++i;
		}

		return docs;
	}

	private static HashMap<String, LinkedList<Mention>> arrangeMentionsByDocs(LinkedList<Mention> mentions) {
		HashMap<String, LinkedList<Mention>> docIdToMentions = new HashMap<>();
		for (Mention m : mentions) {
			LinkedList<Mention> docMentions = docIdToMentions.get(m.docId);
			if (docMentions == null) {
				docMentions = new LinkedList<>();
				docIdToMentions.put(m.docId, docMentions);
			}
			docMentions.add(m);
		}

		return docIdToMentions;
	}
	
	public String text = null;
	public String docId = null;
	public Mention[] mentions = null;
}
