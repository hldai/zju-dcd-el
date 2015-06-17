// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dcd.el.io.IOUtils;
import dcd.el.objects.Document;
import dcd.el.objects.Mention;
import dcd.el.tac.Query;

// TODO
public class QueryReader {
	public static final String QUERY_HEAD_PREFIX = "  <query id=";
	public static final String QUERY_END = "  </query>";
	public static final String NAME_PREFIX = "<name>";
	public static final String NAME_SUFFIX = "</name>";

	public static final Pattern QUERY_PATTERN = Pattern
			.compile("\\s*?<query\\sid=\\\"(.*?)\\\">\\s*"
					+ "<name>(.*?)</name>\\s*" + "<docid>(.*?)</docid>\\s*"
					+ "<beg>(.*?)</beg>\\s*" + "<end>(.*?)</end>\\s*"
					+ "</query>\\s*");

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
			docs[docIdx++] = doc;
		}

		return docs;
	}
	
	private static LinkedList<Integer> getNumMentionsList(LinkedList<Query> queries) {
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

			Matcher m = QUERY_PATTERN.matcher(sb);
			if (m.find()) {
				Query q = new Query();
				q.queryId = m.group(1);
				q.name = m.group(2);
				q.docId = m.group(3);
				q.begPos = Integer.valueOf(m.group(4));
				q.endPos = Integer.valueOf(m.group(5));

				return q;
			}
			// line = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public QueryReader(String fileName) {
		reader = IOUtils.getUTF8BufReader(fileName);
	}

	public Query nextQuery() {
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
