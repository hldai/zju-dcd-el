// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dcd.el.ELConsts;
import dcd.el.io.IOUtils;
import dcd.el.objects.ByteArrayString;

// the linking basis of a mention
// the data needed to link a mention
public class LinkingBasisMention {
	public void toFile(DataOutputStream dos) {
		IOUtils.writeStringAsByteArr(dos, queryId, ELConsts.QUERY_ID_BYTE_LEN);
		try {
			dos.writeInt(numCandidates);
			
			for (int i = 0; i < numCandidates; ++i) {
//				IOUtils.writeStringAsByteArr(dos, mids[i], ELConsts.MID_BYTE_LEN);
				mids[i].toFileWithFixedLen(dos, ELConsts.MID_BYTE_LEN);
				dos.writeFloat(aliasLikelihoods[i]);
				dos.writeFloat(popularities[i]);
				dos.writeDouble(tfidfSimilarities[i]);
				dos.writeDouble(evScores[i]);
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
			mids = new ByteArrayString[numCandidates];
			aliasLikelihoods = new float[numCandidates];
			popularities = new float[numCandidates];
			tfidfSimilarities = new double[numCandidates];
			evScores = new double[numCandidates];
			for (int i = 0; i < numCandidates; ++i) {
//				mids[i] = IOUtils.readStringInByteArr(dis, ELConsts.MID_BYTE_LEN);
				mids[i] = new ByteArrayString();
				mids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
				aliasLikelihoods[i] = dis.readFloat();
				popularities[i] = dis.readFloat();
				tfidfSimilarities[i] = dis.readDouble();
				evScores[i] = dis.readDouble();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String queryId = null;
	public int numCandidates = 0;
	
	public ByteArrayString[] mids = null;
	public float[] aliasLikelihoods = null;
	public float[] popularities = null;
	public double[] tfidfSimilarities = null;
	public double[] evScores = null;
}
