package dcd.el.tac;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import dcd.el.ELConsts;
import dcd.el.io.IOUtils;
import dcd.el.objects.ByteArrayString;

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

	private ByteArrayString[] mids = null;
	private ByteArrayString[] eids = null;
}
