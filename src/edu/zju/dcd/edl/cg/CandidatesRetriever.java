package edu.zju.dcd.edl.cg;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.utils.AdjGpeMapper;
import edu.zju.dcd.edl.utils.CommonUtils;

public class CandidatesRetriever {
	private static final String FORUM_AUTHOR_TAG = "author=\"";
	
	private static final float DEF_PSE = 1e-6f;
	
	public static class CandidateWithPopularity implements Comparable<CandidateWithPopularity> {
		public ByteArrayString mid = null;
		public float likelihood = 0;
		public float popularity = 0;
		public float npse = 0;
		
		@Override
		public int compareTo(CandidateWithPopularity cwp) {
			float thisPop = likelihood * popularity,
					cmpPop = cwp.popularity * cwp.likelihood;
			if (thisPop > cmpPop) {
				return -1;
			} else if (thisPop == cmpPop) {
				return 0;
			}
			
			return 1;
		}
	}
	
	public static class CandidatesOfMention {
		public CandidateWithPopularity[] candidates = null;
	}
	
	public static class CandidateWithPse {
		public ByteArrayString mid = null;
		public float pse = DEF_PSE;
		public float npse = DEF_PSE;
	}
	
	public static class CandidatesWithPseOfMention {
		LinkedList<CandidateWithPse> candidatesWithPse = null;
	}
	
	public static class CandidatesTemporary {
		public LinkedList<ByteArrayString> mids = null;
		public LinkedList<Float> pses = null;
	}
	
	private static class MidPopularity {
		public MidPopularity(String midPopularityFileName) {
			System.out.println("loading mid_popularities...");
			DataInputStream dis = IOUtils.getBufferedDataInputStream(midPopularityFileName);
			try {
				int numMids = dis.readInt();
				System.out.println(numMids);
				mids = new ByteArrayString[numMids];
				popularities = new float[numMids];
				
				for (int i = 0; i < numMids; ++i) {
					mids[i] = new ByteArrayString();
					mids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
					popularities[i] = dis.readFloat();
				}
				
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("done.");
		}
		
		public float getPopularity(ByteArrayString mid) {
			int pos = Arrays.binarySearch(mids, mid);
			if (pos < 0)
				return 0;
			return popularities[pos];
		}
		
		public ByteArrayString[] mids;
		public float[] popularities;
	}
	
	public CandidatesRetriever(AliasDict aliasDict, String midPopularityFileName) {
		this.aliasDict = aliasDict;
		midPopularity = new MidPopularity(midPopularityFileName);
		adjGpeMapper = new AdjGpeMapper("d:/data/cr/nation_adj.txt");
	}
	
	public CandidatesRetriever(IndexedAliasDictWithPse indexedAliasDictWithPse, String midPopularityFileName,
			String personListFileName) {
		this.indexedAliasDictWithPse = indexedAliasDictWithPse;
		midPopularity = new MidPopularity(midPopularityFileName);
		if (personListFileName != null) {
			loadPersonList(personListFileName);
		}
		adjGpeMapper = new AdjGpeMapper("d:/data/cr/nation_adj.txt");
	}
	
	public CandidatesOfMention[] getCandidatesInDocument(Document doc) {
		CandidatesWithPseOfMention[] candidatesWithPseInDoc = getCandidatesWithPseInDoc(doc);
		
		return getCandidatesOfMentions(candidatesWithPseInDoc);

//		CandidatesTemporary[] tmpCandidates = new CandidatesTemporary[doc.mentions.length];
//		if (aliasDict != null) {
//			getCandidatesInDocumentAliasDict(doc, tmpCandidates);
//		} else {
//			getCandidatesInDocumentAliasDictWithPse(doc, tmpCandidates);
//		}
//		
//		return getCandidatesOfMentionsFromTempCandidates(tmpCandidates);
	}
	
	private CandidatesOfMention[] getCandidatesOfMentions(CandidatesWithPseOfMention[] candidatesWithPseInDoc) {
		CandidatesOfMention[] candidatesOfMentions = new CandidatesOfMention[candidatesWithPseInDoc.length];
		int pos = 0;
		for (CandidatesWithPseOfMention tmpCandidates : candidatesWithPseInDoc) {
			CandidatesOfMention candidatesOfMention = new CandidatesOfMention();
			candidatesOfMentions[pos++] = candidatesOfMention;
			
			if (tmpCandidates.candidatesWithPse == null || tmpCandidates.candidatesWithPse.size() == 0) {
				continue;
			}
			
			candidatesOfMention.candidates = new CandidateWithPopularity[tmpCandidates.candidatesWithPse.size()];
			int ix = 0;
			for (CandidateWithPse  curCandidate : tmpCandidates.candidatesWithPse) {
				CandidateWithPopularity candidateWithPopularity = new CandidateWithPopularity();
				candidatesOfMention.candidates[ix++] = candidateWithPopularity;
				candidateWithPopularity.mid = curCandidate.mid;
				candidateWithPopularity.likelihood = curCandidate.pse;
				candidateWithPopularity.popularity = midPopularity.getPopularity(curCandidate.mid);
				candidateWithPopularity.npse = curCandidate.npse;
			}
			
			Arrays.sort(candidatesOfMention.candidates);
		}
		
		return candidatesOfMentions;
	}
	
	private CandidatesOfMention[] getCandidatesOfMentionsFromTempCandidates(CandidatesTemporary[] tmpCandidates) {
		CandidatesOfMention[] candidatesOfMentions = new CandidatesOfMention[tmpCandidates.length];
		int pos = 0;
		for (CandidatesTemporary tmpCandidate : tmpCandidates) {
			
			CandidatesOfMention candidatesOfMention = new CandidatesOfMention();
			candidatesOfMentions[pos++] = candidatesOfMention;
			
			if (tmpCandidate.mids == null || tmpCandidate.mids.size() == 0) {
				continue;
			}
			
			candidatesOfMention.candidates = new CandidateWithPopularity[tmpCandidate.mids.size()];
			Iterator<ByteArrayString> midIter = tmpCandidate.mids.iterator();
			Iterator<Float> pseIter = tmpCandidate.pses.iterator();
			int ix = 0;
			while (midIter.hasNext()) {
				ByteArrayString mid = midIter.next();
				float pse = pseIter.next();
				CandidateWithPopularity candidateWithPopularity = new CandidateWithPopularity();
				candidatesOfMention.candidates[ix++] = candidateWithPopularity;
				candidateWithPopularity.mid = mid;
				candidateWithPopularity.popularity = pse * midPopularity.getPopularity(mid);
			}
			
			Arrays.sort(candidatesOfMention.candidates);
		}
		
		return candidatesOfMentions;
	}
	
	private void getCandidatesInDocumentAliasDict(Document doc, CandidatesTemporary[] candidates) {
		for (int i = 0; i < doc.mentions.length; ++i) {
			CandidatesTemporary tmpCandidates = new CandidatesTemporary();
			String curNameString = doc.mentions[i].nameString;
			
			
//			boolean isPerson = false;
//			for (int j = i - 1; j > -1; --j) {
//				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)) {
//					mergeMids(tmpCandidates.mids, candidates[j].mids);
//					if (candidates[j].mids != null && candidates[j].mids.size() > 0) {
//						isPerson = isMidPerson(candidates[j].mids.getFirst());
//						if (isPerson) {
//							System.out.println(curNameString);
//						}
//					}
//					break;
//				}
//			}
//			
//			if (!isPerson) {
//				LinkedList<ByteArrayString> curMids = aliasDict.getMids(curNameString);
//				mergeMids(tmpCandidates.mids, curMids);
//			}
			
			tmpCandidates.mids = aliasDict.getMids(curNameString);
			for (int j = 0; j < i; ++j) {
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)) {
					mergeMids(tmpCandidates.mids, candidates[j].mids);
				}
			}
			
			candidates[i] = tmpCandidates;
		}
	}
	
	private CandidatesWithPseOfMention[] getCandidatesWithPseInDoc(Document doc) {
		CandidatesWithPseOfMention[] candidatesWithPseInDoc = new CandidatesWithPseOfMention[doc.mentions.length];
		
		boolean[] isForumPosters = new boolean[doc.mentions.length];
		for (int i = 0; i < doc.mentions.length; ++i) {
			CandidatesWithPseOfMention curCandidatesWithPse = new CandidatesWithPseOfMention();
			curCandidatesWithPse.candidatesWithPse = new LinkedList<CandidateWithPse>();
			candidatesWithPseInDoc[i] = curCandidatesWithPse;
			
			if (doc.text != null && doc.mentions[i].beg >= FORUM_AUTHOR_TAG.length() 
					&& doc.text.substring(doc.mentions[i].beg - FORUM_AUTHOR_TAG.length(), 
							doc.mentions[i].beg).equals(FORUM_AUTHOR_TAG)) {
				isForumPosters[i] = true;
				continue;
			}
			
			String curNameString = doc.mentions[i].nameString;

			if (adjGpeMapper != null) {
				String adjName = adjGpeMapper.getName(curNameString);
				if (adjName != null) {
					curNameString = adjName;
				}
			}
			
			for (int j = 0; j < i && !isForumPosters[i]; ++j) {
				if (isForumPosters[j] && doc.mentions[j].nameString.equals(curNameString)) {
					isForumPosters[i] = true;
				}
			}
			if (isForumPosters[i]) {
				continue;
			}
			
			getCandidatesWithPseOfMention(doc, curNameString, candidatesWithPseInDoc, i);
//			getCandidatesWithPseOfMentionSimple(doc, curNameString, candidatesWithPseInDoc, i);
		}
		
		return candidatesWithPseInDoc;
	}
	
	private void getCandidatesWithPseOfMentionSimple(Document doc, String curNameString,
			CandidatesWithPseOfMention[] candidatesWithPseInDoc, int curMentionsPos) {
		CandidatesWithPseOfMention curCandidatesWithPse = candidatesWithPseInDoc[curMentionsPos];
		
		IndexedAliasDictWithPse.MidPseList midPseList = indexedAliasDictWithPse.getMidPses(curNameString);
		if (midPseList != null) {
			CandidatesWithPseOfMention tmpCandidateWithPse = new CandidatesWithPseOfMention();
			tmpCandidateWithPse.candidatesWithPse = new LinkedList<CandidateWithPse>();
			Iterator<ByteArrayString> midIter = midPseList.mids.iterator();
			Iterator<Float> pseIter = midPseList.pses.iterator();
			Iterator<Float> npseIter = midPseList.npses.iterator();
			while (midIter.hasNext()) {
				ByteArrayString mid = midIter.next();
				float pse = pseIter.next();
				float npse = npseIter.next();
				
				CandidateWithPse candidateWithPse = new CandidateWithPse();
				candidateWithPse.mid = mid;
				candidateWithPse.pse = pse;
				candidateWithPse.npse = npse;
				tmpCandidateWithPse.candidatesWithPse.add(candidateWithPse);
			}
			
			mergeCandidatesWithPseOfMention(curCandidatesWithPse, tmpCandidateWithPse, false);
		}
	}
	
	private void getCandidatesWithPseOfMention(Document doc, String curNameString,
			CandidatesWithPseOfMention[] candidatesWithPseInDoc, int curMentionsPos) {
		CandidatesWithPseOfMention curCandidatesWithPse = candidatesWithPseInDoc[curMentionsPos];
		boolean isPerson = false;
		int j = 0;
		for (j = curMentionsPos - 1; j > -1; --j) {
			if (doc.mentions[curMentionsPos].beg >= doc.mentions[j].beg && doc.mentions[curMentionsPos].end <= doc.mentions[j].end) {
				continue;
			}
			
			if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)) {
				if (candidatesWithPseInDoc[j].candidatesWithPse != null 
						&& candidatesWithPseInDoc[j].candidatesWithPse.size() > 0) {
					isPerson = isMidPerson(candidatesWithPseInDoc[j].candidatesWithPse.getFirst().mid);
					if (!isPerson && candidatesWithPseInDoc[j].candidatesWithPse.size() > 1) {
						isPerson = isMidPerson(candidatesWithPseInDoc[j].candidatesWithPse.get(1).mid);
					}
				}
				
				mergeCandidatesWithPseOfMention(curCandidatesWithPse, candidatesWithPseInDoc[j], false);
				
				break;
			}
		}
		
		if (!isPerson) {
			IndexedAliasDictWithPse.MidPseList midPseList = indexedAliasDictWithPse.getMidPses(curNameString);
			
			if (midPseList != null) {
				CandidatesWithPseOfMention tmpCandidateWithPse = new CandidatesWithPseOfMention();
				tmpCandidateWithPse.candidatesWithPse = new LinkedList<CandidateWithPse>();
				Iterator<ByteArrayString> midIter = midPseList.mids.iterator();
				Iterator<Float> pseIter = midPseList.pses.iterator();
				Iterator<Float> npseIter = midPseList.npses.iterator();
				while (midIter.hasNext()) {
					ByteArrayString mid = midIter.next();
					float pse = pseIter.next();
					float npse = npseIter.next();
					
					CandidateWithPse candidateWithPse = new CandidateWithPse();
					candidateWithPse.mid = mid;
					candidateWithPse.pse = pse;
					candidateWithPse.npse = npse;
					tmpCandidateWithPse.candidatesWithPse.add(candidateWithPse);
				}
				
				mergeCandidatesWithPseOfMention(curCandidatesWithPse, tmpCandidateWithPse, false);
			}
		}
	}
	
	private void getCandidatesInDocumentAliasDictWithPse(Document doc, CandidatesTemporary[] candidates) {
		boolean[] isForumPosters = new boolean[doc.mentions.length];
		for (int i = 0; i < doc.mentions.length; ++i) {
			CandidatesTemporary tmpCandidates = new CandidatesTemporary();
			candidates[i] = tmpCandidates;
			String curNameString = doc.mentions[i].nameString;
			
			tmpCandidates.mids = new LinkedList<ByteArrayString>();
			tmpCandidates.pses = new LinkedList<Float>();
			
			if (doc.mentions[i].beg >= FORUM_AUTHOR_TAG.length() 
					&& doc.text.substring(doc.mentions[i].beg - FORUM_AUTHOR_TAG.length(), 
							doc.mentions[i].beg).equals(FORUM_AUTHOR_TAG)) {
				isForumPosters[i] = true;
				continue;
			}

			for (int j = 0; j < i && !isForumPosters[i]; ++j) {
				if (isForumPosters[j] && doc.mentions[j].nameString.equals(curNameString)) {
					isForumPosters[i] = true;
				}
			}
			if (isForumPosters[i]) {
				continue;
			}
			
			boolean isPerson = false;
			for (int j = i - 1; j > -1; --j) {
//				if (curNameString.equals("Walters")) {
//					System.out.println(i + "\t" + j + "\t" + doc.mentions[j].nameString);
//				}
				
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)) {
//					if (curNameString.equals("Walters")) {
//						System.out.println("has " + doc.mentions[j].nameString);
//					}
					mergeMidPses(tmpCandidates.mids, tmpCandidates.pses,
							candidates[j].mids, candidates[j].pses);

					if (candidates[j].mids != null && candidates[j].mids.size() > 0) {
						isPerson = isMidPerson(candidates[j].mids.getFirst());
						if (!isPerson && candidates[j].mids.size() > 1) {
							isPerson = isMidPerson(candidates[j].mids.get(1));
						}
						
//						if (curNameString.equals("Walters")) {
//							System.out.println(i + "\t" + candidates[j].mids.getFirst().toString().trim() + "\t" + isPerson);
//						}
					}
					break;
				}
			}
			
			if (!isPerson) {
				IndexedAliasDictWithPse.MidPseList midPseList = indexedAliasDictWithPse.getMidPses(curNameString);
				
				if (midPseList != null) {
					mergeMidPses(tmpCandidates.mids, tmpCandidates.pses, midPseList.mids,
							midPseList.pses);
//					tmpCandidates.mids = midPseList.mids;
//					tmpCandidates.pses = midPseList.pses;
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
	
	private static void mergeCandidatesWithPseOfMention(CandidatesWithPseOfMention mainCandidates, 
			CandidatesWithPseOfMention newCandidates, boolean useDefPseForNewCandidate) {
		if (newCandidates == null || newCandidates.candidatesWithPse == null)
			return ;
		
		LinkedList<CandidateWithPse> prevCandidates = mainCandidates.candidatesWithPse;
		
		mainCandidates.candidatesWithPse = new LinkedList<CandidateWithPse>();
		for (CandidateWithPse candidateWithPse : prevCandidates) {
			mainCandidates.candidatesWithPse.add(candidateWithPse);
		}
		
		for (CandidateWithPse candidateWithPse : newCandidates.candidatesWithPse) {
			boolean isIn = false;
			for (CandidateWithPse tmp : prevCandidates) {
				if (candidateWithPse.mid.equals(tmp.mid)) {
					tmp.pse = tmp.pse > candidateWithPse.pse ? tmp.pse : candidateWithPse.pse;
					isIn = true;
					break;
				}
			}
			
			if (!isIn) {
				CandidateWithPse newCandidateWithPse = new CandidateWithPse();
				newCandidateWithPse.mid = candidateWithPse.mid;
				if (useDefPseForNewCandidate) {
					newCandidateWithPse.pse = DEF_PSE;
				} else {
					newCandidateWithPse.pse = candidateWithPse.pse;
				}
				newCandidateWithPse.npse = candidateWithPse.npse;
				mainCandidates.candidatesWithPse.add(newCandidateWithPse);
			}
		}
	}
	
	private static void mergeMidPses(LinkedList<ByteArrayString> mainMidList, LinkedList<Float> mainPseList,
			LinkedList<ByteArrayString> newMidList, LinkedList<Float> newPseList) {
		if (newMidList == null)
			return ;
		
		if (mainMidList == null)
			return ;
		if (mainPseList == null)
			return ;
		
		Iterator<ByteArrayString> midIteratorNew = newMidList.iterator();
		Iterator<Float> pseIteratorNew = newPseList.iterator();
		while (midIteratorNew.hasNext()) {
			ByteArrayString newMid = midIteratorNew.next();
			float newPse = pseIteratorNew.next();
			
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
	
	private void loadPersonList(String fileName) {
		int numLines = IOUtils.getNumLinesFor(fileName);
		personMids = new String[numLines];
		BufferedReader reader = IOUtils.getUTF8BufReader(fileName);
		try {
			for (int i = 0; i < numLines; ++i) {
				personMids[i] = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Arrays.sort(personMids);
	}
	
	private boolean isMidPerson(ByteArrayString mid) {
		String smid = mid.toString().trim();
		int pos = Arrays.binarySearch(personMids, smid);
		return pos > -1;
	}
	
	AliasDict aliasDict = null;
	IndexedAliasDictWithPse indexedAliasDictWithPse = null;
	MidPopularity midPopularity = null;
	
	AdjGpeMapper adjGpeMapper = null;
	
	String[] personMids = null;
}
