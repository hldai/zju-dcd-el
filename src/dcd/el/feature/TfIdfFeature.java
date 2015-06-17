package dcd.el.feature;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TfIdfFeature extends Feature {
	@Override
	public boolean fromFile(RandomAccessFile raf) {
		try {
			if (raf.getFilePointer() >= raf.length())
				return false;

//			if (withWid)
//				wid = raf.readInt();

			int len = raf.readInt();
			if (len == 0) {
				termIndices = null;
				values = null;
				return true;
			}
//			System.out.println(len);
			termIndices = new int[len];
			values = new double[len];

			FileChannel fc = raf.getChannel();
			ByteBuffer buf = ByteBuffer.allocate(len * Integer.BYTES);
			buf.clear();
			fc.read(buf);
			buf.rewind();
			buf.asIntBuffer().get(termIndices);

			// for (int i = len - 10; i < len; ++i) {
			// System.out.println("term " + termIndices[i]);
			// }

			buf = ByteBuffer.allocate(len * Double.BYTES);
			buf.clear();
			fc.read(buf);
			buf.rewind();
			buf.asDoubleBuffer().get(values);

			// for (int i = 0; i < 10; ++i) {
			// System.out.println("term " + values[i]);
			// }
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

			int numTerms = dis.readInt();
			if (numTerms == 0) {
				termIndices = null;
				values = null;
				return true;
			}

			termIndices = new int[numTerms];
			values = new double[numTerms];

			for (int i = 0; i < numTerms; ++i)
				termIndices[i] = dis.readInt();
			for (int i = 0; i < numTerms; ++i)
				values[i] = dis.readDouble();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void toFile(RandomAccessFile raf) {
		if (values == null)
			toFile(raf, 0);
		toFile(raf, values.length);
	}

	@Override
	public void toFile(DataOutputStream dos) {
		if (values == null)
			toFile(dos, 0);
		toFile(dos, values.length);
	}

	public void toFile(RandomAccessFile raf, int numTerms) {
		try {
//			if (withWid)
//				raf.writeInt(wid);
			raf.writeInt(numTerms);
			if (numTerms == 0)
				return;

			FileChannel outChannel = raf.getChannel();

			ByteBuffer buf = ByteBuffer.allocate(numTerms * Integer.BYTES);
			buf.clear();
			buf.asIntBuffer().put(termIndices, 0, numTerms);
			outChannel.write(buf);

			buf = ByteBuffer.allocate(numTerms * Double.BYTES);
			buf.clear();
			buf.asDoubleBuffer().put(values, 0, numTerms);
			outChannel.write(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void toFile(DataOutputStream dos, int numTerms) {
		try {
//			if (withWid)
//				dos.writeInt(wid);
			dos.writeInt(numTerms);
			if (numTerms == 0)
				return;

			for (int i = 0; i < numTerms; ++i) {
				dos.writeInt(termIndices[i]);
			}

			for (int i = 0; i < numTerms; ++i) {
				dos.writeDouble(values[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static double similarity(TfIdfFeature fl, TfIdfFeature fr) {
		if (fl.termIndices == null || fr.termIndices == null) {
			return 0;
		}

		double result = 0;
		int posl = 0, posr = 0;
		while (posl < fl.values.length && posr < fr.values.length) {
			int terml = fl.termIndices[posl], termr = fr.termIndices[posr];
			if (terml < termr) {
				++posl;
			} else if (terml > termr) {
				++posr;
			} else {
				result += fl.values[posl] * fr.values[posr];
				++posl;
				++posr;
			}
		}
		
		result /= fl.getNorm() * fr.getNorm();

		return result;
	}

	public double getNorm() {
		double result = 0;
		for (double val : values) {
			result += val * val;
		}
		return Math.sqrt(result);
	}

	public int[] termIndices = null;
	public double[] values = null;
}
