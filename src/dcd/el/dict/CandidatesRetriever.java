package dcd.el.dict;

import java.util.Iterator;
import java.util.LinkedList;

import dcd.el.objects.ByteArrayString;
import dcd.el.objects.Document;
import dcd.el.utils.CommonUtils;

public class CandidatesRetriever {
	public static class Candidates {
		public LinkedList<ByteArrayString> mids = null;
		public LinkedList<Float> pses = null;
	}
	
	public CandidatesRetriever(AliasDict aliasDict) {
		this.aliasDict = aliasDict;
	}
	
	public CandidatesRetriever(IndexedAliasDictWithPse indexedAliasDictWithPse) {
		this.indexedAliasDictWithPse = indexedAliasDictWithPse;
	}
	
	public Candidates[] getCandidatesInDocument(Document doc) {
		Candidates[] candidates = new Candidates[doc.mentions.length];
		
		if (aliasDict != null) {
			getCandidatesInDocumentAliasDict(doc, candidates);
		} else {
			getCandidatesInDocumentAliasDictWithPse(doc, candidates);
		}
		
		return candidates;
	}
	
	public void getCandidatesInDocumentAliasDict(Document doc, Candidates[] candidates) {
		for (int i = 0; i < doc.mentions.length; ++i) {
			Candidates tmpCandidates = new Candidates();
			String curNameString = doc.mentions[i].nameString;
			tmpCandidates.mids = aliasDict.getMids(curNameString);
			for (int j = 0; j < i; ++j) {
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)) {
					mergeMids(tmpCandidates.mids, candidates[j].mids);
				}
			}
			
			candidates[i] = tmpCandidates;
		}
	}
	
	public void getCandidatesInDocumentAliasDictWithPse(Document doc, Candidates[] candidates) {
		for (int i = 0; i < doc.mentions.length; ++i) {
			Candidates tmpCandidates = new Candidates();
			candidates[i] = tmpCandidates;
			String curNameString = doc.mentions[i].nameString;
			IndexedAliasDictWithPse.MidPseList midPseList = indexedAliasDictWithPse.getMidPses(curNameString);
			
			if (midPseList != null) {
				tmpCandidates.mids = midPseList.mids;
				tmpCandidates.pses = midPseList.pses;
			}
			
			for (int j = 0; j < i; ++j) {
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)) {
					mergeMidPses(tmpCandidates.mids, tmpCandidates.pses,
							candidates[j].mids, candidates[j].pses);
				}
			}
		}
	}
	
	private static void mergeMids(LinkedList<ByteArrayString> mainMidList, LinkedList<ByteArrayString> newList) {
		if (newList == null)
			return ;
		
		if (mainMidList == null)
			mainMidList = new LinkedList<ByteArrayString>();
		
		for (ByteArrayString newMid : newList) {
			boolean flg = true;
			for (ByteArrayString mid : mainMidList) {
				if (mid.compareTo(newMid) == 0) {
					flg = false;
					break;
				}
			}
			if (flg)
				mainMidList.add(newMid);
		}
	}
	
	private static void mergeMidPses(LinkedList<ByteArrayString> mainMidList, LinkedList<Float> mainPseList,
			LinkedList<ByteArrayString> newMidList, LinkedList<Float> newPseList) {
		if (newMidList == null)
			return ;
		
		if (mainMidList == null)
			mainMidList = new LinkedList<ByteArrayString>();
		if (mainPseList == null)
			mainPseList = new LinkedList<Float>();
		
		Iterator<ByteArrayString> midIterator = newMidList.iterator();
		Iterator<Float> pseIterator = newPseList.iterator();
		while (midIterator.hasNext()) {
			ByteArrayString newMid = midIterator.next();
			float newPse = pseIterator.next();
			boolean flg = true;
			for (ByteArrayString mid : mainMidList) {
				if (mid.compareTo(newMid) == 0) {
					flg = false;
					break;
				}
			}
			if (flg) {
				mainMidList.add(newMid);
				mainPseList.add(newPse); // TODO use a small const? 
			}
		}
	}
	
	AliasDict aliasDict = null;
	IndexedAliasDictWithPse indexedAliasDictWithPse = null;
}
