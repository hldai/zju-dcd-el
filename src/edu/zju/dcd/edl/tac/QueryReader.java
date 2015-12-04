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

	// note that the text attribute of a Document is not set in this method
	public static Document[] toDocuments(String queryFileName) {
		BufferedReader reader = IOUtils.getUTF8BufReader(queryFileName);

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

		if (queries.size() == 0)
			return null;

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
				m.queryId = q.queryId;
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

		if (queries.size() == 0)
			return null;
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
					m1 = QUERY_PATTERN.matcher(sb);
			Query q = new Query();
			Matcher m = null;
			if (m1.find()) {
				q.begPos = Integer.valueOf(m1.group(4));
				q.endPos = Integer.valueOf(m1.group(5));
				m = m1;
			} else if (m0.find()) {
				m = m0;
			}
			
			if (m != null) {
				q.queryId = m.group(1);
				q.name = m.group(2);
				q.docId = m.group(3);
				return q;
			}
//			Matcher m = withPosition ? QUERY_PATTERN.matcher(sb) : QUERY_PATTERN_WITHOUT_POS.matcher(sb);
//			if (m.find()) {
//				Query q = new Query();
//				q.queryId = m.group(1);
//				q.name = m.group(2);
//				q.docId = m.group(3);
//				
//				if (withPosition) {
//					q.begPos = Integer.valueOf(m.group(4));
//					q.endPos = Integer.valueOf(m.group(5));
//				}
//
//				return q;
//			}
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
