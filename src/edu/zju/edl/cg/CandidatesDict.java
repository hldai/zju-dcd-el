package edu.zju.edl.cg;

import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.obj.ByteArrayString;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;


public class CandidatesDict {
	public static class CandidatesEntry {
		public ByteArrayString[] mids = null;
		public float[] cmns = null; // commonness
	}

	public CandidatesDict(String candidatesDictFile) {
		System.out.println(String.format("Loading %s ...", candidatesDictFile));
//		ByteArrayString dstName = new ByteArrayString("clinton");

		DataInputStream dis = IOUtils.getBufferedDataInputStream(candidatesDictFile);
		try {
			int numNames = dis.readInt();
			int totalNumCandidates = dis.readInt();
			System.out.println(String.format("%d names", numNames));

			int curPos = 0;
			names = new ByteArrayString[numNames];
			begPos = new int[numNames];
			lens = new short[numNames];
			mids = new ByteArrayString[totalNumCandidates];
			cmns = new float[totalNumCandidates];
			for (int i = 0; i < numNames; ++i) {
				names[i] = new ByteArrayString();
				names[i].fromFileWithByteLen(dis);

				if (i > 0)
					assert names[i].compareTo(names[i - 1]) > 0;

				begPos[i] = curPos;
				lens[i] = dis.readShort();

				for (short j = 0; j < lens[i]; ++j) {
					mids[curPos] = new ByteArrayString();
					mids[curPos].fromFileWithByteLen(dis);
					cmns[curPos] = dis.readFloat();
					++curPos;

//					if (names[i].compareTo(dstName) == 0)
//						System.out.println(mids[curPos - 1].toString());
				}
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}

	public CandidatesEntry getCandidates(String name) {
		String lcname = name.toLowerCase();
		CandidatesEntry candidatesEntry = getCandidates(new ByteArrayString(lcname));
		if (candidatesEntry == null && lcname.startsWith("the ")) {
			candidatesEntry = getCandidates(new ByteArrayString(lcname.substring(4).trim()));
		}
		return candidatesEntry;
	}

	public CandidatesEntry getCandidates(ByteArrayString name) {
		int idx = Arrays.binarySearch(names, name);
		if (idx < 0)
			return null;

		CandidatesEntry entry = new CandidatesEntry();
		entry.mids = Arrays.copyOfRange(mids, begPos[idx], begPos[idx] + lens[idx]);
		entry.cmns = Arrays.copyOfRange(cmns, begPos[idx], begPos[idx] + lens[idx]);
		return entry;
	}

	ByteArrayString[] names = null;
	int[] begPos = null;
	short[] lens = null;
	ByteArrayString[] mids = null;
	float[] cmns = null; // commonness
}
