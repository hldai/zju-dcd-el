package edu.zju.dcd.edl.cg;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.tac.MidToEidMapper;
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
//			float thisPop = likelihood * popularity,
//					cmpPop = cwp.popularity * cwp.likelihood;
			float thisPop = npse,
					cmpPop = cwp.npse;
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
	
	public CandidatesRetriever(IndexedAliasDictWithPse indexedAliasDictWithPse, String midPopularityFileName,
			String personListFileName, String nameDictFile, MidToEidMapper mteMapper) {
		this.indexedAliasDictWithPse = indexedAliasDictWithPse;
		
		if (midPopularityFileName != null)
			midPopularity = new MidPopularity(midPopularityFileName);
		
		if (personListFileName != null)
			loadPersonList(personListFileName);
		
//		if (nameDictFile != null)
//			adjGpeMapper = new AdjGpeMapper(nameDictFile);

		if (nameDictFile != null)
			loadNameDictFile(nameDictFile);
		
		this.mteMapper = mteMapper;
	}
	
	public CandidatesOfMention[] getCandidatesInDocument(Document doc) {
		CandidatesWithPseOfMention[] candidatesWithPseInDoc = getCandidatesWithPseInDoc(doc);
		
		CandidatesOfMention[] candidatesOfMentions = getCandidatesOfMentions(candidatesWithPseInDoc);
		
		return candidatesOfMentions;
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
			
			LinkedList<CandidateWithPopularity> candidatesList = new LinkedList<CandidateWithPopularity>();
//			int ix = 0;
			for (CandidateWithPse curCandidate : tmpCandidates.candidatesWithPse) {
				boolean flg = true;
				for (CandidateWithPopularity cwp : candidatesList) {
					if (curCandidate.mid.compareTo(cwp.mid) == 0) {
						flg = false;
					}
				}
//				for (int i = 0; i < ix && flg; ++i) {
//				}
				
				if (!flg)
					continue;
				
				CandidateWithPopularity candidateWithPopularity = new CandidateWithPopularity();
//				candidatesOfMention.candidates[ix++] = candidateWithPopularity;
				candidateWithPopularity.mid = curCandidate.mid;
				candidateWithPopularity.likelihood = curCandidate.pse;
				candidateWithPopularity.popularity = -1;
				if (midPopularity != null)
					candidateWithPopularity.popularity = midPopularity.getPopularity(curCandidate.mid);
				candidateWithPopularity.npse = curCandidate.npse;
				
				candidatesList.add(candidateWithPopularity);
			}
			
			candidatesOfMention.candidates = new CandidateWithPopularity[candidatesList.size()];
			candidatesList.toArray(candidatesOfMention.candidates);
			
			Arrays.sort(candidatesOfMention.candidates);
		}
		
		return candidatesOfMentions;
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

//			if (adjGpeMapper != null) {
//				String adjName = adjGpeMapper.getName(curNameString);
//				if (adjName != null) {
//					curNameString = adjName;
//				}
//			}
			if (aliasToOrigin != null) {
				String originName = aliasToOrigin.get(curNameString);
				if (originName != null) {
//					System.out.println(String.format("%s -> %s", curNameString, originName));
					curNameString = originName;
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

//			if (curNameString.equals("Africa")) {
//				System.out.println("hit");
//				for (CandidateWithPse cwp : curCandidatesWithPse.candidatesWithPse) {
//					System.out.println(String.format("%s\t%.6f", cwp.mid.toString().trim(), cwp.npse));
//				}
//			}
//			getCandidatesWithPseOfMentionSimple(doc, curNameString, candidatesWithPseInDoc, i);
		}
		
		return candidatesWithPseInDoc;
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

				// TODO trying
				if (isPerson)
					mergeCandidatesWithPseOfMention(curCandidatesWithPse, candidatesWithPseInDoc[j], false);

//				if (curNameString.equals("Africa")) {
//					System.out.println("hit0");
//					for (CandidateWithPse cwp : curCandidatesWithPse.candidatesWithPse) {
//						System.out.println(String.format("%s\t%.6f", cwp.mid.toString().trim(), cwp.npse));
//					}
//				}
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

					// TODO emadr
//					if (mteMapper != null && mteMapper.getEid(mid) == null) {
//						continue;
//					}

					// TODO debug
//					if (curNameString.equals("Africa")) {
//						System.out.println("in\t" + mid.toString().trim());
//					}
					
					CandidateWithPse candidateWithPse = new CandidateWithPse();
					candidateWithPse.mid = mid;
					candidateWithPse.pse = pse;
					candidateWithPse.npse = npse;
					tmpCandidateWithPse.candidatesWithPse.add(candidateWithPse);
				}
				
				mergeCandidatesWithPseOfMention(curCandidatesWithPse, tmpCandidateWithPse, false);


//				if (curNameString.equals("Africa")) {
//					System.out.println("hit1");
//					for (CandidateWithPse cwp : curCandidatesWithPse.candidatesWithPse) {
//						System.out.println(String.format("%s\t%.6f", cwp.mid.toString().trim(), cwp.npse));
//					}
//				}
			}
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
	
	private void loadPersonList(String fileName) {
		System.out.println(String.format("Loading %s ...", fileName));
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

	private void loadNameDictFile(String nameDictFile) {
		BufferedReader reader = IOUtils.getUTF8BufReader(nameDictFile);
		aliasToOrigin = new HashMap<>();
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split("\t");
				if (vals.length < 3)
					continue;

				for (int i = 2; i < vals.length; ++i) {
					aliasToOrigin.put(vals[i], vals[0]);
//					System.out.println(vals[i] + "\t" + vals[0]);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	AliasDict aliasDict = null;
	IndexedAliasDictWithPse indexedAliasDictWithPse = null;
	MidPopularity midPopularity = null;
	// TODO remove
//	AdjGpeMapper adjGpeMapper = null;
	HashMap<String, String> aliasToOrigin = null;
	
	String[] personMids = null;

	// for when NIL's are removed
	protected MidToEidMapper mteMapper = null;
}
