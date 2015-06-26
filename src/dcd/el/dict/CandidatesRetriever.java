package dcd.el.dict;

import java.util.LinkedList;

import dcd.el.objects.ByteArrayString;
import dcd.el.objects.Document;
import dcd.el.utils.CommonUtils;

public class CandidatesRetriever {
	public static class Candidates {
		public LinkedList<ByteArrayString> mids = null;
	}
	
	public CandidatesRetriever(AliasDict dict) {
		this.dict = dict;
	}
	
	public Candidates[] getCandidatesInDocument(Document doc) {
		Candidates[] candidates = new Candidates[doc.mentions.length];
		
		for (int i = 0; i < doc.mentions.length; ++i) {
			Candidates tmpCandidates = new Candidates();
			String curNameString = doc.mentions[i].nameString;
			tmpCandidates.mids = dict.getMids(curNameString);
			for (int j = 0; j < i; ++j) {
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)) {
					mergeMids(tmpCandidates.mids, candidates[j].mids);
				}
			}
			
			candidates[i] = tmpCandidates;
		}
		
		return candidates;
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
	
	AliasDict dict = null;
}
