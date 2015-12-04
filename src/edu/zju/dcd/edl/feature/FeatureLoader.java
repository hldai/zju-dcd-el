// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.feature;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;

public class FeatureLoader {
	private class FeaturePackSortEntry {
		public long filePointer = -1;
		public FeaturePack featPack = null;
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

	public FeaturePack loadFeatures(String mid) {
		int pos = Arrays.binarySearch(mids, mid);
		if (pos < 0)
			return null;

		try {
			featFileRaf.seek(filePointers[pos]);
			FeaturePack feats = new FeaturePack();
			featFileRaf.readInt(); // ignore wid
//			feats.wid = featFileRaf.readInt();
//			feats.popularity.fromFile(featFileRaf);
			feats.tfidf.fromFile(featFileRaf);
			return feats;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	// TODO for mids whose features are already loaded, reloading is not needed.
	public FeaturePack[] loadFeaturePacks(LinkedList<ByteArrayString> candidateMids) {
		FeaturePack[] featPacks = new FeaturePack[candidateMids.size()];
		FeaturePackSortEntry[] sortEntries = new FeaturePackSortEntry[candidateMids
				.size()];

		int notNullCnt = 0, cnt = 0;
		for (ByteArrayString mid : candidateMids) {
			int pos = Arrays.binarySearch(mids, mid);

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

		Arrays.sort(sortEntries, 0, notNullCnt, fpComparator);

		try {
			for (int i = 0; i < notNullCnt; ++i) {
				FeaturePackSortEntry sortEntry = sortEntries[i];
				featFileRaf.seek(sortEntry.filePointer);
				featFileRaf.readInt(); // ignore wid
//				sortEntry.featPack.popularity.fromFile(featFileRaf);
				sortEntry.featPack.tfidf.fromFile(featFileRaf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return featPacks;
	}
	
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

		Arrays.sort(sortEntries, 0, notNullCnt, fpComparator);

		try {
			for (int i = 0; i < notNullCnt; ++i) {
				FeaturePackSortEntry sortEntry = sortEntries[i];
				featFileRaf.seek(sortEntry.filePointer);
				featFileRaf.readInt(); // ignore wid
//				sortEntry.featPack.popularity.fromFile(featFileRaf);
				sortEntry.featPack.tfidf.fromFile(featFileRaf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return featPacks;
	}

	RandomAccessFile featFileRaf = null;
	ByteArrayString[] mids = null;
	long[] filePointers = null;

	FilePointerComparator fpComparator = new FilePointerComparator();
}
