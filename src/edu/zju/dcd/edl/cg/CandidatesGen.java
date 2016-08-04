package edu.zju.dcd.edl.cg;

import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.Mention;
import edu.zju.dcd.edl.utils.CommonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by dhl on 16-7-31.
 */
public class CandidatesGen {
	private static final String FORUM_AUTHOR_TAG = "author=\"";

	public CandidatesGen(CandidatesDict candidatesDict, String personListFileName, String nameDictFile) {
		this.candidatesDict = candidatesDict;

		if (personListFileName != null)
			loadPersonList(personListFileName);

		if (nameDictFile != null)
			loadNameDictFile(nameDictFile);
	}

	public CandidatesDict.CandidatesEntry[] getCandidatesOfMentionsInDoc(Document doc, int[] corefChain) {
		CandidatesDict.CandidatesEntry[] candidatesEntries = new CandidatesDict.CandidatesEntry[doc.mentions.length];

		Arrays.fill(corefChain, -1);

		boolean[] isForumPosters = findForumPosters(doc, corefChain);
		handleFullNames(doc, isForumPosters, candidatesEntries, corefChain);

		for (int i = 0; i < doc.mentions.length; ++i) {
			if (isForumPosters[i] || candidatesEntries[i] != null)
				continue;

			String curNameString = doc.mentions[i].nameString;
//			if (curNameString.equals("Iraq")) {
//				System.out.println("iraq");
//			}

			// closest
			boolean flg = false;
			for (int j = 0; !flg && j < doc.mentions.length; ++j) {
				if (i != j && CommonUtils.isAbbr(doc.mentions[j].nameString, curNameString)
						&& candidatesEntries[j] != null) {
					candidatesEntries[i] = candidatesEntries[j];
					flg = true;
					if (i > j)
						corefChain[i] = j;
					else
						corefChain[j] = i;
				}
			}
			if (flg)
				continue;

			for (int j = i - 1; !flg && j >= 0; --j) {
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)
						&& isPerson(candidatesEntries[j])) {
//				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)
//						&& doc.mentions[j].entityType.equals("PER")) {
					candidatesEntries[i] = candidatesEntries[j];
//					System.out.println(String.format("%s -> %s", curNameString, doc.mentions[j].nameString));
					flg = true;
					corefChain[i] = j;
				}
			}
			if (flg)
				continue;

			candidatesEntries[i] = candidatesDict.getCandidates(curNameString);
		}

		fixNilCorefChain(candidatesEntries, doc.mentions, corefChain);

		return candidatesEntries;
	}

	private void fixNilCorefChain(CandidatesDict.CandidatesEntry[] candidatesEntries, Mention[] mentions,
								  int[] corefChain) {
		for (int i = 0; i < candidatesEntries.length; ++i) {
			if (candidatesEntries[i] == null && corefChain[i] < 0) {
				for (int j = 0; j < i; ++j) {
					if (mentions[i].nameString.equals(mentions[j].nameString)) {
						corefChain[i] = j;
					}
				}
			}
		}
	}

	private void handleFullNames(Document doc, boolean[] isForumPosters,
								 CandidatesDict.CandidatesEntry[] candidatesEntries,
								 int[] corefChain) {
		for (int i = 0; i < doc.mentions.length; ++i) {
			if (isForumPosters[i])
				continue;

			String curNameString = doc.mentions[i].nameString;

			if (aliasToOrigin != null) {
				String originName = aliasToOrigin.get(curNameString);
				if (originName != null) {
//					System.out.println(String.format("%s -> %s", curNameString, originName));
					int prevMentionPos = findName(doc.mentions, i, curNameString);
					if (prevMentionPos > -1) {
						candidatesEntries[i] = candidatesEntries[prevMentionPos];
						corefChain[i] = prevMentionPos;
					} else {
						curNameString = originName;
						candidatesEntries[i] = candidatesDict.getCandidates(curNameString);
					}
					continue;
				}
			}

			boolean isFullName = true;
			for (int j = 0; j < doc.mentions.length && isFullName; ++j) {
				if (j == i)
					continue;
				if (CommonUtils.hasWord(doc.mentions[j].nameString, curNameString)
						|| CommonUtils.isAbbr(doc.mentions[j].nameString, curNameString)) {
					isFullName = false;
				}
			}

			if (isFullName) {
				corefChain[i] = findName(doc.mentions, i, curNameString);
				if (corefChain[i] > -1) {
					candidatesEntries[i] = candidatesEntries[corefChain[i]];
				} else {
					candidatesEntries[i] = candidatesDict.getCandidates(curNameString);
				}
			}
		}
	}

	private int findName(Mention[] mentions, int endPos, String name) {
		for (int i = 0; i < endPos; ++i) {
			if (mentions[i].nameString.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private boolean[] findForumPosters(Document doc, int[] corefChain) {
		boolean[] isForumPosters = new boolean[doc.mentions.length];
		for (int i = 0; i < doc.mentions.length; ++i) {
			if (doc.text != null && doc.mentions[i].beg >= FORUM_AUTHOR_TAG.length()
					&& doc.text.substring(doc.mentions[i].beg - FORUM_AUTHOR_TAG.length(),
					doc.mentions[i].beg).equals(FORUM_AUTHOR_TAG)) {
				isForumPosters[i] = true;
			}
		}

		for (int i = 0; i < doc.mentions.length; ++i) {
			String curNameString = doc.mentions[i].nameString;
			for (int j = 0; j < doc.mentions.length; ++j) {
				if (i != j && isForumPosters[j] && doc.mentions[j].nameString.equals(curNameString)) {
					isForumPosters[i] = true;
				}
			}
		}

		for (int i = 1; i < doc.mentions.length; ++i) {
			String curNameString = doc.mentions[i].nameString;
			for (int j = 0; j < i; ++j) {
				if (isForumPosters[i] && isForumPosters[j] && doc.mentions[j].nameString.equals(curNameString)) {
					corefChain[i] = j;
					break;
				}
			}
		}

		return isForumPosters;
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

	private boolean isPerson(CandidatesDict.CandidatesEntry candidatesEntry) {
		if (candidatesEntry == null)
			return false;
		for (int i = 0; i < 2 && i < candidatesEntry.mids.length; ++i) {
			if (isMidPerson(candidatesEntry.mids[i]))
				return true;
		}
		return false;
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

	// adj gpe
	HashMap<String, String> aliasToOrigin = null;

	String[] personMids = null;

	CandidatesDict candidatesDict = null;
}
