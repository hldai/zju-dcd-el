// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.feature;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.tac.LinkingBasisMention;

// rename as AttributeLoader
public class FeatureLoader {
	private static final int MAX_CACHE_SIZE = 1000000;

	private static class FeaturePackSortEntry {
		public long filePointer = -1;
		public FeaturePack featPack = null;
	}

	private static class CacheEntry {
		public int midPos;
		public TfIdfFeature tfidf = null;
		public CacheEntry prev = null;
		public CacheEntry next = null;
	}

	private static class CacheQueue {
		public CacheQueue() {
			head.next = tail;
			tail.prev = head;
		}

		public CacheEntry add(int midPos, TfIdfFeature tfidf) {
			CacheEntry cacheEntry = new CacheEntry();
			cacheEntry.midPos = midPos;
			cacheEntry.tfidf = tfidf;

			add(cacheEntry);
			return cacheEntry;
		}

		public void add(CacheEntry cacheEntry) {
			cacheEntry.next = head.next;
			head.next.prev = cacheEntry;
			head.next = cacheEntry;
			cacheEntry.prev = head;
			++len;
		}

		public void remove(CacheEntry cacheEntry) {
			cacheEntry.prev.next = cacheEntry.next;
			cacheEntry.next.prev = cacheEntry.prev;
			cacheEntry.prev = null;
			cacheEntry.next = null;
		}

		public CacheEntry pop() {
			CacheEntry last = tail.prev;
			if (last == head)
				return null;

			remove(last);
			return last;
		}

		public int len = 0;
		public CacheEntry head = new CacheEntry();
		public CacheEntry tail = new CacheEntry();
	}

	private class FilePointerComparator implements
			Comparator<FeaturePackSortEntry> {
		@Override
		public int compare(FeaturePackSortEntry el, FeaturePackSortEntry er) {
			if (el.filePointer < er.filePointer)
				return -1;
			if (el.filePointer == er.filePointer)
				return 0;
			return 1;
		}
	}

	public FeatureLoader(String featFileName, String featIndexFileName) {
		try {
			featFileRaf = new RandomAccessFile(featFileName, "r");

			System.out.println("Loading index from " + featIndexFileName + " ...");
			DataInputStream dis = IOUtils.getBufferedDataInputStream(featIndexFileName);
			int len = dis.readInt();
			System.out.println(len);
			mids = new ByteArrayString[len];
			filePointers = new long[len];
			for (int i = 0; i < len; ++i) {
				mids[i] = new ByteArrayString();
				mids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
				filePointers[i] = dis.readLong();
			}
			dis.close();
			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			featFileRaf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public FeaturePack loadFeatures(String mid) {
//		int pos = Arrays.binarySearch(mids, mid);
//		if (pos < 0)
//			return null;
//
//		try {
//			featFileRaf.seek(filePointers[pos]);
//			FeaturePack feats = new FeaturePack();
//			featFileRaf.readInt(); // ignore wid
////			feats.wid = featFileRaf.readInt();
////			feats.popularity.fromFile(featFileRaf);
//			feats.tfidf.fromFile(featFileRaf);
//			return feats;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}
	
	// TODO for mids whose features are already loaded, reloading is not needed.
//	public FeaturePack[] loadFeaturePacks(LinkedList<ByteArrayString> candidateMids) {
//		FeaturePack[] featPacks = new FeaturePack[candidateMids.size()];
//		FeaturePackSortEntry[] sortEntries = new FeaturePackSortEntry[candidateMids
//				.size()];
//
//		int notNullCnt = 0, cnt = 0;
//		for (ByteArrayString mid : candidateMids) {
//			int pos = Arrays.binarySearch(mids, mid);
//
//			if (pos < 0) {
//				featPacks[cnt] = null;
//			} else {
//				featPacks[cnt] = new FeaturePack();
//				sortEntries[notNullCnt] = new FeaturePackSortEntry();
//				sortEntries[notNullCnt].featPack = featPacks[cnt];
//				sortEntries[notNullCnt].filePointer = filePointers[pos];
//				++notNullCnt;
//			}
//			++cnt;
//		}
//
//		Arrays.sort(sortEntries, 0, notNullCnt, fpComparator);
//
//		try {
//			for (int i = 0; i < notNullCnt; ++i) {
//				FeaturePackSortEntry sortEntry = sortEntries[i];
//				featFileRaf.seek(sortEntry.filePointer);
//				featFileRaf.readInt(); // ignore wid
////				sortEntry.featPack.popularity.fromFile(featFileRaf);
//				sortEntry.featPack.tfidf.fromFile(featFileRaf);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return featPacks;
//	}
	
	// TODO for mids whose features are already loaded, reloading is not needed.
	public FeaturePack[] loadFeaturePacks(CandidatesRetriever.CandidateWithPopularity[] candidates,
			int maxNumCandidates) {
		int numCandidates = maxNumCandidates < candidates.length ? maxNumCandidates : candidates.length;
		FeaturePack[] featPacks = new FeaturePack[numCandidates];
		FeaturePackSortEntry[] sortEntries = new FeaturePackSortEntry[numCandidates];

		int notNullCnt = 0, cnt = 0;
		for (int i = 0; i < numCandidates; ++i) {
			int pos = Arrays.binarySearch(mids, candidates[i].mid);

			if (pos < 0) {
				featPacks[cnt] = null;
			} else {
				featPacks[cnt] = new FeaturePack();

				sortEntries[notNullCnt] = new FeaturePackSortEntry();
				sortEntries[notNullCnt].featPack = featPacks[cnt];
				sortEntries[notNullCnt].filePointer = filePointers[pos];
				++notNullCnt;
			}
			++cnt;
		}

		loadFeatures(sortEntries, notNullCnt);

		return featPacks;
	}

	public FeaturePack[] loadFeaturePacks(ByteArrayString[] candidateMids) {
		int numCandidates = candidateMids.length;
		FeaturePack[] featPacks = new FeaturePack[numCandidates];
		FeaturePackSortEntry[] sortEntries = new FeaturePackSortEntry[numCandidates];

		int notNullCnt = 0, cnt = 0;
		for (int i = 0; i < numCandidates; ++i) {
			int pos = Arrays.binarySearch(mids, candidateMids[i]);

			if (pos < 0) {
				featPacks[cnt] = null;
			} else {
				featPacks[cnt] = new FeaturePack();
//				sortEntries[notNullCnt] = new FeaturePackSortEntry();
//				sortEntries[notNullCnt].featPack = featPacks[cnt];
//				sortEntries[notNullCnt].filePointer = filePointers[pos];
//				++notNullCnt;

				CacheEntry cacheEntry = cacheMap.get(pos);
				if (cacheEntry != null) {
//					System.out.println("hit");
					featPacks[cnt].tfidf = cacheEntry.tfidf;
					cacheQueue.remove(cacheEntry);
					cacheQueue.add(cacheEntry);
				} else {
					sortEntries[notNullCnt] = new FeaturePackSortEntry();
					sortEntries[notNullCnt].featPack = featPacks[cnt];
					sortEntries[notNullCnt].filePointer = filePointers[pos];
					++notNullCnt;

					cacheEntry = cacheQueue.add(pos, featPacks[cnt].tfidf);
					cacheMap.put(pos, cacheEntry);
					if (cacheQueue.len > MAX_CACHE_SIZE) {
						CacheEntry toRemove = cacheQueue.pop();
						cacheMap.remove(toRemove.midPos);
					}
				}
			}
			++cnt;
		}

		loadFeatures(sortEntries, notNullCnt);

		return featPacks;
	}

	public FeaturePack[][] loadFeaturePacks(LinkedList<LinkingBasisMention> scoresToGet) {
		FeaturePack[][] featurePacks = new FeaturePack[scoresToGet.size()][];
		LinkedList<FeaturePackSortEntry> toLoadEntries = new LinkedList<>();
		int i = 0;
		for (LinkingBasisMention sm : scoresToGet) {
			featurePacks[i] = new FeaturePack[sm.mids.length];

			for (int j = 0; j < sm.mids.length; ++j) {
				int midPos = Arrays.binarySearch(mids, sm.mids[j]);
				if (midPos < 0) {
					featurePacks[i][j] = null;
				} else {
					featurePacks[i][j] = new FeaturePack();
					handleMid(midPos, featurePacks[i][j], toLoadEntries);
				}
			}
			++i;
		}

		loadFeatures(toLoadEntries);

		return featurePacks;
	}

	private void handleMid(int midPos, FeaturePack dstFeaturePack, LinkedList<FeaturePackSortEntry> toLoadEntries) {
		CacheEntry cacheEntry = cacheMap.get(midPos);
		if (cacheEntry != null) {
//			System.out.println("hit");
			dstFeaturePack.tfidf = cacheEntry.tfidf;
			cacheQueue.remove(cacheEntry);
			cacheQueue.add(cacheEntry);
		} else {
			FeaturePackSortEntry entry = new FeaturePackSortEntry();
			entry.featPack = dstFeaturePack;
			entry.filePointer = filePointers[midPos];
			toLoadEntries.add(entry);

			cacheEntry = cacheQueue.add(midPos, dstFeaturePack.tfidf);
			cacheMap.put(midPos, cacheEntry);
			if (cacheQueue.len > MAX_CACHE_SIZE) {
				CacheEntry toRemove = cacheQueue.pop();
				cacheMap.remove(toRemove.midPos);
			}
		}
	}

	private void loadFeatures(LinkedList<FeaturePackSortEntry> toLoadEntries) {
		Collections.sort(toLoadEntries, fpComparator);

		try {
			for (FeaturePackSortEntry entry : toLoadEntries) {
				featFileRaf.seek(entry.filePointer);
				featFileRaf.readInt(); // ignore wid
//				sortEntry.featPack.popularity.fromFile(featFileRaf);
				entry.featPack.tfidf.fromFile(featFileRaf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFeatures(FeaturePackSortEntry[] sortEntries, int numEntries) {
		Arrays.sort(sortEntries, 0, numEntries, fpComparator);

		try {
			for (int i = 0; i < numEntries; ++i) {
				FeaturePackSortEntry sortEntry = sortEntries[i];
				featFileRaf.seek(sortEntry.filePointer);
				featFileRaf.readInt(); // ignore wid
//				sortEntry.featPack.popularity.fromFile(featFileRaf);
				sortEntry.featPack.tfidf.fromFile(featFileRaf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	CacheQueue cacheQueue = new CacheQueue();
	HashMap<Integer, CacheEntry> cacheMap = new HashMap<>();

	RandomAccessFile featFileRaf = null;
	ByteArrayString[] mids = null;
	long[] filePointers = null;

	FilePointerComparator fpComparator = new FilePointerComparator();
}
