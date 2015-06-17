package dcd.el.feature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import dcd.el.io.IOUtils;

public class FeatureLoader {
	private class FeaturePackSortHelper {
		public long filePointer = -1;
		public FeaturePack featPack = null;
	}

	private class FilePointerComparator implements
			Comparator<FeaturePackSortHelper> {
		@Override
		public int compare(FeaturePackSortHelper hl, FeaturePackSortHelper hr) {
			if (hl.filePointer < hr.filePointer)
				return -1;
			if (hl.filePointer == hr.filePointer)
				return 0;
			return 1;
		}
	}

	public FeatureLoader(String featFileName, String featIndexFileName) {
		try {
			featFileRaf = new RandomAccessFile(featFileName, "r");

			System.out.println("Loading index...");
			int numLines = IOUtils.getNumLinesFor(featIndexFileName);
			mids = new String[numLines];
			filePointers = new long[numLines];
			BufferedReader reader = IOUtils.getUTF8BufReader(featIndexFileName);
			String line = null;
			for (int i = 0; i < numLines; ++i) {
				line = reader.readLine();
				String[] vals = line.split("\t");
				mids[i] = vals[0];
				filePointers[i] = Long.valueOf(vals[1]);
			}
			reader.close();
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
			feats.popularity.fromFile(featFileRaf);
			feats.tfidf.fromFile(featFileRaf);
			return feats;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	// probably useless
	public FeaturePack[] loadFeaturePacks(LinkedList<String> candidateMids) {
		FeaturePack[] featPacks = new FeaturePack[candidateMids
				.size()];
		FeaturePackSortHelper[] sortHelpers = new FeaturePackSortHelper[candidateMids
				.size()];

		int notNullCnt = 0, cnt = 0;
		for (String mid : candidateMids) {
			int pos = Arrays.binarySearch(mids, mid);

			if (pos < 0) {
				featPacks[cnt] = null;
			} else {
				featPacks[cnt] = new FeaturePack();
				sortHelpers[notNullCnt] = new FeaturePackSortHelper();
				sortHelpers[notNullCnt].featPack = featPacks[cnt];
				sortHelpers[notNullCnt].filePointer = filePointers[pos];
				++notNullCnt;
			}
			++cnt;
		}

		Arrays.sort(sortHelpers, 0, notNullCnt, fpComparator);

		try {
			for (int i = 0; i < notNullCnt; ++i) {
				FeaturePackSortHelper sortHelper = sortHelpers[i];
				featFileRaf.seek(sortHelper.filePointer);
				featFileRaf.readInt(); // ignore wid
//				sortHelper.featPack.wid = featFileRaf.readInt();
				sortHelper.featPack.popularity.fromFile(featFileRaf);
				sortHelper.featPack.tfidf.fromFile(featFileRaf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return featPacks;
	}

	RandomAccessFile featFileRaf = null;
	String[] mids = null;
	long[] filePointers = null;

	FilePointerComparator fpComparator = new FilePointerComparator();
}
