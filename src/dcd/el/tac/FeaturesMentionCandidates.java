// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dcd.el.ELConsts;
import dcd.el.io.IOUtils;

// features of the candidates of a mention
public class FeaturesMentionCandidates {
	public void toFile(DataOutputStream dos) {
		IOUtils.writeStringAsByteArr(dos, queryId, ELConsts.QUERY_ID_BYTE_LEN);
		try {
			dos.writeInt(numCandidates);
			
			for (int i = 0; i < numCandidates; ++i) {
				IOUtils.writeStringAsByteArr(dos, mids[i], ELConsts.MID_BYTE_LEN);
				dos.writeFloat(popularities[i]);
				dos.writeDouble(tfidfSimilarities[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean fromFile(DataInputStream dis) {
		try {
			if (dis.available() <= 0)
				return false;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		queryId = IOUtils.readStringInByteArr(dis, ELConsts.QUERY_ID_BYTE_LEN);
		try {
			numCandidates = dis.readInt();
			mids = new String[numCandidates];
			popularities = new float[numCandidates];
			tfidfSimilarities = new double[numCandidates];
			for (int i = 0; i < numCandidates; ++i) {
				mids[i] = IOUtils.readStringInByteArr(dis, ELConsts.MID_BYTE_LEN);
				popularities[i] = dis.readFloat();
				tfidfSimilarities[i] = dis.readDouble();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String queryId = null;
	public int numCandidates = 0;
	
	public String[] mids = null;
	public float[] popularities = null;
	public double[] tfidfSimilarities = null;
}
