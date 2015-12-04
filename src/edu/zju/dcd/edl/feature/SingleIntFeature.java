// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.feature;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SingleIntFeature extends Feature {

	@Override
	public boolean fromFile(RandomAccessFile raf) {
		try {
			if (raf.getFilePointer() >= raf.length())
				return false;

//			if (withWid)
//				wid = raf.readInt();
			value = raf.readInt();
//			System.out.println(wid + "\t" + value + "\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean fromFile(DataInputStream dis) {
		try {
			if (dis.available() <= 0)
				return false;

//			if (withWid)
//				wid = dis.readInt();
			value = dis.readInt();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void toFile(DataOutputStream dos) {
		try {
//			if (withWid)
//				dos.writeInt(wid);
			dos.writeInt(value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toFile(RandomAccessFile raf) {
		try {
//			if (withWid)
//				raf.writeInt(wid);
			raf.writeInt(value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int value;
}
