package dcd.el.dict;

import java.util.LinkedList;

import dcd.el.objects.Document;
import dcd.el.utils.CommonUtils;

public class CandidatesRetriever {
	public static class Candidates {
		public LinkedList<String> mids = null;
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
	
	private static void mergeMids(LinkedList<String> mainMidList, LinkedList<String> newList) {
		if (newList == null)
			return ;
		
		if (mainMidList == null)
			mainMidList = new LinkedList<String>();
		
		for (String newMid : newList) {
			boolean flg = true;
			for (String mid : mainMidList) {
				if (mid.equals(newMid)) {
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
