// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.tac;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.Mention;

// TODO
public class QueryReader {
	public static final String QUERY_HEAD_PREFIX = "  <query id=";
	public static final String QUERY_END = "  </query>";
	public static final String NAME_PREFIX = "<name>";
	public static final String NAME_SUFFIX = "</name>";

	private static final Pattern QUERY_PATTERN = Pattern
			.compile("\\s*?<query\\sid=\\\"(.*?)\\\">\\s*"
					+ "<name>(.*?)</name>\\s*" + "<docid>(.*?)</docid>\\s*"
					+ "<beg>(.*?)</beg>\\s*" + "<end>(.*?)</end>\\s*"
					+ "</query>\\s*");

	private static final Pattern QUERY_PATTERN_WITHOUT_POS = Pattern
			.compile("\\s*?<query\\sid=\\\"(.*?)\\\">\\s*"
					+ "<name>(.*?)</name>\\s*"
					+ "<docid>(.*?)</docid>\\s*</query>\\s*");

	private static final Pattern QUERY_PATTERN_WITHOUT_POS_TRAIN = Pattern
			.compile("\\s*?<query\\sid=\\\"(.*?)\\\">\\s*"
					+ "<name>(.*?)</name>\\s*"
					+ "<docid>(.*?)</docid>\\s*<entity>(.*?)</entity>\\s*</query>\\s*");

	// note that the text attribute of a Document is not set in this method
	public static Document[] toDocumentsXmlFile(String mentionsFile) {
		LinkedList<Query> queries = readQueries(mentionsFile);
		return queriesToDocs(queries);
	}

	// [RUN_ID]\t[QUERY_ID]\t[MENTION_NAME]\t[DOC_ID]:[BEG_POS]:[END_POS]\t ...
	public static Document[] toDocumentsEdlFile(String mentionsFile) {
		LinkedList<Query> queries = readQueriesTabFile(mentionsFile);
		return queriesToDocs(queries);
	}

	private static Document[] queriesToDocs(LinkedList<Query> queries) {
		Collections.sort(queries, new Query.QueryComparator());

		LinkedList<Integer> numMentionsList = getNumMentionsList(queries);

		Mention.MentionPosComparator mentionPosComparator = new Mention.MentionPosComparator();

		int numDocs = numMentionsList.size();
		Document[] docs = new Document[numDocs];
		Iterator<Query> iter = queries.iterator();
		int docIdx = 0;
		for (int n : numMentionsList) {
			Document doc = new Document();
			doc.mentions = new Mention[n];
			Query q = null;
			for (int i = 0; i < n; ++i) {
				q = iter.next();
				Mention m = new Mention();
				m.mentionId = q.queryId;
				m.nameString = q.name;
				m.beg = q.begPos;
				m.end = q.endPos;
				doc.mentions[i] = m;
			}
			doc.docId = q.docId;
			Arrays.sort(doc.mentions, mentionPosComparator);

			docs[docIdx++] = doc;
		}

		return docs;
	}

//	public static LinkedList<Mention> loadMentionsEdlFile(String edlFile) {
//		BufferedReader reader = IOUtils.getUTF8BufReader(edlFile);
//		LinkedList<Mention> mentions = new LinkedList<>();
//		String line = null;
//		try {
//			while ((line = reader.readLine()) != null) {
//				Mention m = mentionFromEdlLine(line);
//				mentions.add(m);
//			}
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return mentions;
//	}

//	private static Mention mentionFromEdlLine(String line) {
//		String[] vals = line.split("\t");
//
//		Mention m = new Mention();
//		m.mentionId = vals[1];
//		m.nameString = vals[2];
//
//		int colonPos = vals[3].indexOf(':'), dashPos = vals[3].indexOf('-');
//		m.docId = vals[3].substring(0, colonPos);
//		m.beg = Integer.valueOf(vals[3].substring(colonPos + 1, dashPos));
//		m.end = Integer.valueOf(vals[3].substring(dashPos + 1));
//
//		m.kbid = vals[4];
//		m.entityType = vals[5];
//		m.mentionType = vals[6];
//
//		return m;
//	}
	
	public static LinkedList<Query> readQueries(String fileName) {
		BufferedReader reader = IOUtils.getUTF8BufReader(fileName);

		LinkedList<Query> queries = new LinkedList<Query>();
		Query query = null;
		try {
			while ((query = nextQuery(reader)) != null) {
				queries.add(query);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		assert queries.size() != 0;
		return queries;
	}

	private static LinkedList<Query> readQueriesTabFile(String fileName) {
		BufferedReader reader = IOUtils.getUTF8BufReader(fileName);
		LinkedList<Query> queries = new LinkedList<Query>();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split("\t");
				Query q = new Query();
				q.queryId = vals[1];
				q.name = vals[2];
//				System.out.println(q.name);

				int colonPos = vals[3].indexOf(':'), dashPos = vals[3].indexOf('-');
				q.docId = vals[3].substring(0, colonPos);
				q.begPos = Integer.valueOf(vals[3].substring(colonPos + 1, dashPos));
				q.endPos = Integer.valueOf(vals[3].substring(dashPos + 1));
				queries.add(q);
//				System.out.println(q.docId);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return queries;
	}

	private static LinkedList<Integer> getNumMentionsList(
			LinkedList<Query> queries) {
		int mentionCnt = 0;
		LinkedList<Integer> numMentionsList = new LinkedList<Integer>();
		String preDocId = null;
		for (Query q : queries) {
			if (preDocId != null && !q.docId.equals(preDocId)) {
				numMentionsList.add(mentionCnt);
				mentionCnt = 0;
			}
			++mentionCnt;
			preDocId = q.docId;
		}
		numMentionsList.add(mentionCnt);

		return numMentionsList;
	}

	// TODO
	private static Query nextQuery(BufferedReader bufReader) {
		String line = null;
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = bufReader.readLine()) != null
					&& !line.startsWith(QUERY_HEAD_PREFIX))
				;

			sb.append(line);
			while ((line = bufReader.readLine()) != null) {
				sb.append(line);
				if (line.equals(QUERY_END))
					break;
			}

			if (line == null)
				return null;

			Matcher m0 = QUERY_PATTERN_WITHOUT_POS.matcher(sb),
					m1 = QUERY_PATTERN.matcher(sb), 
					m2 = QUERY_PATTERN_WITHOUT_POS_TRAIN.matcher(sb);
			Query q = new Query();
			Matcher m = null;
			if (m1.find()) {
				q.begPos = Integer.valueOf(m1.group(4));
				q.endPos = Integer.valueOf(m1.group(5));
				m = m1;
			} else if (m0.find()) {
				m = m0;
			} else if (m2.find()) {
				m = m2;
			}
			
			if (m != null) {
				q.queryId = m.group(1);
				q.name = m.group(2);
				q.docId = m.group(3);
				return q;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public QueryReader(String fileName) {
		reader = IOUtils.getUTF8BufReader(fileName);
	}

	public Query nextQuery(boolean withPosition) {
		return nextQuery(reader);
	}

	public void close() {
		try {
			if (reader != null)
				reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedReader reader = null;
}
