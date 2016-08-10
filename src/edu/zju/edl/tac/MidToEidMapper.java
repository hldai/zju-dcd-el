// author: DHL brnpoem@gmail.com

package edu.zju.edl.tac;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.zju.edl.ELConsts;
import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.obj.ByteArrayString;

// map mid to eid
// mid: entity id of freebase
// eid: entity id of the 2014 KB
public class MidToEidMapper {
	public MidToEidMapper(String midToEidFileName) {
		System.out.println("Loading mid to eid file...");
		DataInputStream dis = IOUtils.getBufferedDataInputStream(midToEidFileName);
		
		try {
			int len = dis.readInt();
			mids = new ByteArrayString[len];
			eids = new ByteArrayString[len];
			for (int i = 0; i < len; ++i) {
				mids[i] = new ByteArrayString();
				mids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
				eids[i] = new ByteArrayString();
				eids[i].fromFileWithFixedLen(dis, ELConsts.EID14_BYTE_LEN);
			}

			dis.close();
			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getEid(String mid) {
		ByteArrayString bas = new ByteArrayString(mid);
		int pos = Arrays.binarySearch(mids, bas);
		
		if (pos < 0)
			return null;
		
		return eids[pos].toString();
	}
	
	public String getEid(ByteArrayString mid) {
		int pos = Arrays.binarySearch(mids, mid);
		if (pos < 0)
			return null;
		return eids[pos].toString();
	}

	private ByteArrayString[] mids = null;
	private ByteArrayString[] eids = null;
}
