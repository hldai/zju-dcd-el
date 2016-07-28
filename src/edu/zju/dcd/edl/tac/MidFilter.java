package edu.zju.dcd.edl.tac;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;

public class MidFilter {
	public MidFilter(String fileName) {
		System.out.println("Loading mids need filter...");
		DataInputStream dis = IOUtils.getBufferedDataInputStream(fileName);
		try {
			int numMids = dis.readInt();
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
