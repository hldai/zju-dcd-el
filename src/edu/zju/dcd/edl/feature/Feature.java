// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.feature;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class Feature {
	public static void putEmptyFeature(DataOutputStream dos) {
		try {
			dos.writeInt(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void putEmptyFeature(RandomAccessFile raf) {
		try {
			raf.writeInt(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public abstract boolean fromFile(RandomAccessFile raf);
	public abstract boolean fromFile(DataInputStream dis);
	
	public abstract void toFile(RandomAccessFile raf);
	public abstract void toFile(DataOutputStream dos);
}
