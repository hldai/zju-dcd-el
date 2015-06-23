// author: DHL brnpoem@gmail.com

package dcd.el.feature;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SingleFloatFeature extends Feature {

	@Override
	public boolean fromFile(RandomAccessFile raf) {
		try {
			if (raf.getFilePointer() >= raf.length())
				return false;

			value = raf.readFloat();
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

			value = dis.readFloat();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void toFile(DataOutputStream dos) {
		try {
			dos.writeFloat(value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toFile(RandomAccessFile raf) {
		try {
			raf.writeFloat(value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float value;
}
