package edu.zju.dcd.edl.tac;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import edu.zju.dcd.edl.io.IOUtils;

public class QueryList {
	public QueryList(String queryDir) {
		String tabFileName = null, xmlFileName = null;
		
		File dir = new File(queryDir);
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isFile()) {
				if (f.getName().endsWith(".tab")) {
					tabFileName = f.getAbsolutePath();
				} else if (f.getName().endsWith(".xml")) {
					xmlFileName = f.getAbsolutePath();
				}
			}
		}
		
		LinkedList<Query> queriesList = QueryReader.readQueries(xmlFileName);
		queries = new Query[queriesList.size()];
		queriesList.toArray(queries);
		
		BufferedReader reader = IOUtils.getUTF8BufReader(tabFileName);
		try {
			reader.readLine();
			
			for (Query q : queries) {
				String line = reader.readLine();
				String[] vals = line.split("\t");
				if (!vals[0].equals(q.queryId)) {
					System.out.println("ERROR: query id not equal.");
					break;
				}
				
				q.type = vals[2];
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Query getQuery(String qid) {
		Query q = new Query();
		q.queryId = qid;
		int pos = Arrays.binarySearch(queries, q, queryIdComparator);
		return pos > -1 ? queries[pos] : null;
	}
	
//	LinkedList<Query> queries = null;
	public Query[] queries = null; 
	
	private Query.QueryIdComparator queryIdComparator = new Query.QueryIdComparator();
}
