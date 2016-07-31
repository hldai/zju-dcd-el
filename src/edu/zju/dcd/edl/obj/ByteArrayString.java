// author: DHL hldai@outlook.com

package edu.zju.dcd.edl.obj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

// string stored as a byte array
// used to save memory usage
public class ByteArrayString implements Comparable<ByteArrayString> {
	public ByteArrayString() {

	}

	public ByteArrayString(String s) {
		try {
			bytes = s.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public ByteArrayString(String s, int len) {
		bytes = new byte[len];
		try {
			byte[] tmpBytes = s.getBytes("UTF8");
			int idx = 0;
			while (idx < tmpBytes.length) {
				bytes[idx] = tmpBytes[idx];
				++idx;
			}
			
			while (idx < len)
				bytes[idx++] = 0;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int compareTo(ByteArrayString basRight) {
		for (int i = 0; i < bytes.length && i < basRight.bytes.length; ++i) {
			if (bytes[i] < basRight.bytes[i]) {
				return -1;
			}

			if (bytes[i] > basRight.bytes[i]) {
				return 1;
			}
		}

		if (bytes.length == basRight.bytes.length)
			return 0;
		if (bytes.length < basRight.bytes.length) {
			for (int i = bytes.length; i < basRight.bytes.length; ++i) {
				if (basRight.bytes[i] != 0)
					return -1;
			}
			return 0;
		} else {
			for (int i = basRight.bytes.length; i < bytes.length; ++i) {
				if (bytes[i] != 0)
					return 1;
			}
			return 0;
		}
	}

	public void fromString(String s) {
		try {
			bytes = s.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		return new String(bytes);
	}

	public void toFileWithByteLen(DataOutputStream dos) {
		try {
			dos.write(bytes.length);
			dos.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toFileWithFixedLen(DataOutputStream dos, int len) {
		try {
			dos.write(bytes);
			
			int cnt = len - bytes.length;
			while (cnt-- > 0)
				dos.write(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void fromFileWithByteLen(InputStream is) {
		try {
			int len = is.read();
//			System.out.println(len);
			bytes = new byte[len];
			is.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void fromFileWithFixedLen(DataInputStream dis, int len) {
		bytes = new byte[len];
		try {
			dis.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void fromFileWithFixedLen(RandomAccessFile raf, int len) {
		bytes = new byte[len];
		try {
			raf.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] bytes = null;
}
