package edu.zju.edl.tac;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.zju.edl.ELConsts;
import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.obj.ByteArrayString;

public class MidFilter {
	public MidFilter(String fileName) {
		System.out.println("Loading mids need filter...");
		DataInputStream dis = IOUtils.getBufferedDataInputStream(fileName);
		try {
			int numMids = dis.readInt();
			System.out.println(numMids);
			filterMids = new ByteArrayString[numMids];
			for (int i = 0; i < numMids; ++i) {
				filterMids[i] = new ByteArrayString();
				filterMids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
			}
			dis.close();
			System.out.println("done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean needFilter(ByteArrayString mid) {
		int pos = Arrays.binarySearch(filterMids, mid);
		return pos > -1;
	}
	
	ByteArrayString[] filterMids = null;
}
