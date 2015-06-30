// author: DHL brnpoem@gmail.com

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

			int len = raf.readInt();
			if (len == 0) {
				termIndices = null;
//				values = null;
				tfs = null;
				idfs = null;
				return true;
			}
//			System.out.println(len);
			termIndices = new int[len];
//			values = new double[len];
			tfs = new float[len];
			idfs = new float[len];

			FileChannel fc = raf.getChannel();
			ByteBuffer buf = ByteBuffer.allocate(len * Integer.BYTES);
			buf.clear();
			fc.read(buf);
			buf.rewind();
			buf.asIntBuffer().get(termIndices);

			// for (int i = len - 10; i < len; ++i) {
			// System.out.println("term " + termIndices[i]);
			// }

			readFloatArrayWithFileChannel(fc, tfs);
			readFloatArrayWithFileChannel(fc, idfs);

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

			int numTerms = dis.readInt();
			if (numTerms == 0) {
				termIndices = null;
//				values = null;
				tfs = null;
				idfs = null;
				return true;
			}

			termIndices = new int[numTerms];
//			values = new double[numTerms];
			tfs = new float[numTerms];
			idfs = new float[numTerms];

			for (int i = 0; i < numTerms; ++i)
				termIndices[i] = dis.readInt();
			for (int i = 0; i < numTerms; ++i)
				tfs[i] = dis.readFloat();
			for (int i = 0; i < numTerms; ++i)
				idfs[i] = dis.readFloat();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void toFile(RandomAccessFile raf) {
		if (tfs == null)
			toFile(raf, 0);
		toFile(raf, tfs.length);
	}

	@Override
	public void toFile(DataOutputStream dos) {
		if (tfs == null)
			toFile(dos, 0);
		toFile(dos, tfs.length);
	}

	public void toFile(RandomAccessFile raf, int numTerms) {
		try {
			raf.writeInt(numTerms);
			if (numTerms == 0)
				return;

			FileChannel outChannel = raf.getChannel();

			ByteBuffer buf = ByteBuffer.allocate(numTerms * Integer.BYTES);
			buf.clear();
			buf.asIntBuffer().put(termIndices, 0, numTerms);
			outChannel.write(buf);

			writeFloatArrayWithFileChannel(outChannel, tfs, numTerms);
			writeFloatArrayWithFileChannel(outChannel, idfs, numTerms);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public void toFile(DataOutputStream dos, int numTerms) {
		try {
			dos.writeInt(numTerms);
			if (numTerms == 0)
				return;

			for (int i = 0; i < numTerms; ++i) {
				dos.writeInt(termIndices[i]);
			}

			for (int i = 0; i < numTerms; ++i) {
				dos.writeFloat(tfs[i]);
			}
			for (int i = 0; i < numTerms; ++i) {
				dos.writeFloat(idfs[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double tfSimilarity(TfIdfFeature fl, TfIdfFeature fr) {
		return similarity(fl, fr, false);
	}
	
	
	public static double similarity(TfIdfFeature fl, TfIdfFeature fr) {
		return similarity(fl, fr, true);
	}

	private static double similarity(TfIdfFeature fl, TfIdfFeature fr, boolean useIdf) {
		if (fl.termIndices == null || fr.termIndices == null) {
			return 0;
		}

		double result = 0, tmp = 0;
		int posl = 0, posr = 0;
		while (posl < fl.tfs.length && posr < fr.tfs.length) {
			int terml = fl.termIndices[posl], termr = fr.termIndices[posr];
			if (terml < termr) {
				++posl;
			} else if (terml > termr) {
				++posr;
			} else {
//				result += fl.values[posl] * fr.values[posr];
				tmp = fl.tfs[posl] * fr.tfs[posr];
				if (useIdf)
					tmp *= fl.idfs[posl] * fr.idfs[posr];
				result += tmp;
				++posl;
				++posr;
			}
		}
		
		result /= fl.getNorm(useIdf) * fr.getNorm(useIdf);

		return result;
	}
	
	private double getNorm(boolean useIdf) {
		double result = 0;
		for (int i = 0; i < tfs.length; ++i) {
			double tmp = tfs[i] * tfs[i];
			if (useIdf) {
				tmp *= idfs[i] * idfs[i];
			}
			result += tmp;
		}
		return Math.sqrt(result);
	}

	private void writeFloatArrayWithFileChannel(FileChannel fc, float[] vals, int len) {
		ByteBuffer buf = ByteBuffer.allocate(len * Float.BYTES);
		buf.clear();
		buf.asFloatBuffer().put(vals, 0, len);
		try {
			fc.write(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readFloatArrayWithFileChannel(FileChannel fc, float[] vals) {
		ByteBuffer buf = ByteBuffer.allocate(vals.length * Float.BYTES);
		buf.clear();
		try {
			fc.read(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		buf.rewind();
		buf.asFloatBuffer().get(vals);
	}

	public int[] termIndices = null;
	public float[] tfs = null;
	public float[] idfs = null;
//	public double[] values = null;
}
