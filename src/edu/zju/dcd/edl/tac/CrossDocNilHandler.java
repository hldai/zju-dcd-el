package edu.zju.dcd.edl.tac;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.obj.LinkingResult;

public class CrossDocNilHandler {
	private static class NilQueryList {
		String nilId = null;
		LinkedList<Query> queries = new LinkedList<Query>();
	}
	
	public static void handle(LinkedList<LinkingResult> resultList, String queryFileName) {
		System.out.println("Handling cross doc nils...");
		LinkedList<Query> queries = QueryReader.readQueries(queryFileName);
		Collections.sort(queries, new Query.QueryIdComparator());
		LinkedList<NilQueryList> nilQueryLists = new LinkedList<NilQueryList>();
		
		initQueryLists(queries, resultList, nilQueryLists);
		
		HashMap<String, String> qidNilIdMap = new HashMap<String, String>();
		LinkedList<NilQueryList> newNilQueryLists = new LinkedList<NilQueryList>();
		for (NilQueryList nilQueryList : nilQueryLists) {
			boolean mergeFlg = false;
			for (NilQueryList newNilQueryList : newNilQueryLists) {
				mergeFlg = shouldMerge(nilQueryList.queries, newNilQueryList.queries);
				if (mergeFlg) {
					newNilQueryList.queries.addAll(nilQueryList.queries);
					setNilIds(nilQueryList.queries, newNilQueryList.nilId, qidNilIdMap);
					break;
				}
			}
			
			if (!mergeFlg) {
				newNilQueryLists.add(nilQueryList);
				setNilIds(nilQueryList.queries, nilQueryList.nilId, qidNilIdMap);
			}
		}
		
		for (LinkingResult result : resultList) {
			if (result.kbid.startsWith(ELConsts.NIL)) {
				result.kbid = qidNilIdMap.get(result.queryId);
			}
		}
		
		System.out.println("Done.");
	}
	
	private static void setNilIds(LinkedList<Query> queries, String nilId, HashMap<String, String> qidNilIdMap) {
		for (Query q : queries) {
			qidNilIdMap.put(q.queryId, nilId);
		}
	}
	
	private static boolean shouldMerge(LinkedList<Query> queries0, LinkedList<Query> queries1) {
		for (Query q0 : queries0) {
			for (Query q1 : queries1) {
				if (q0.name.toLowerCase().equals(q1.name.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static void initQueryLists(LinkedList<Query> queries, LinkedList<LinkingResult> resultList,
			LinkedList<NilQueryList> nilQueryLists) {
		Iterator<Query> qiter = queries.iterator();
		Iterator<LinkingResult> riter = resultList.iterator();
		while (riter.hasNext()) {
			LinkingResult linkingResult = riter.next();
			Query q = qiter.next();
			
			if (!linkingResult.queryId.equals(q.queryId)) {
				System.out.println(linkingResult.queryId + "\t" + q.queryId);
				System.out.println("query id not equal.");
				break;
			}
			
			if (linkingResult.kbid.startsWith(ELConsts.NIL)) {
				NilQueryList nilQueryList = findNilQueryList(linkingResult.kbid, nilQueryLists);
				if (nilQueryList == null) {
					nilQueryList = new NilQueryList();
					nilQueryLists.add(nilQueryList);
					nilQueryList.nilId = linkingResult.kbid;
				}
				nilQueryList.queries.add(q);
			}
		}
	}
	
	private static NilQueryList findNilQueryList(String nilId, LinkedList<NilQueryList> nilQueryLists) {
		for (NilQueryList nilQueryList : nilQueryLists) {
			if (nilQueryList.nilId.equals(nilId)) {
				return nilQueryList;
			}
		}
		return null;
	}
}
