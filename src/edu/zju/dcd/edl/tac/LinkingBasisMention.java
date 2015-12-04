// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.tac;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.obj.ByteArrayString;

// the linking basis of a mention
// the data needed to link a mention
public class LinkingBasisMention {
	public void toFile(DataOutputStream dos) {
//		IOUtils.writeStringAsByteArr(dos, queryId, ELConsts.QUERY_ID_BYTE_LEN);
		ByteArrayString queryIdBas = new ByteArrayString(queryId);
		queryIdBas.toFileWithByteLen(dos);
		try {
			dos.writeInt(numCandidates);
			
			for (int i = 0; i < numCandidates; ++i) {
				mids[i].toFileWithFixedLen(dos, ELConsts.MID_BYTE_LEN);
				dos.writeFloat(aliasLikelihoods[i]);
				dos.writeFloat(popularities[i]);
				dos.writeFloat(npses[i]);
				dos.writeDouble(tfidfSimilarities[i]);
				dos.writeFloat(probabilities[i]);
				dos.writeFloat(wordHitRates[i]);
//				dos.writeDouble(evScores[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toFile(BufferedWriter writer) {
		try {
			writer.write(queryId + "\n");
			writer.write(numCandidates + "\n");
			for (int i = 0; i < numCandidates; ++i) {
				writer.write(mids[i].toString().trim() + "\t");
				writer.write(aliasLikelihoods[i] + "\t");
				writer.write(popularities[i] + "\t");
				writer.write(npses[i] + "\t");
				writer.write(tfidfSimilarities[i] + "\t");
				writer.write(probabilities[i] + "\t");
				writer.write(wordHitRates[i] + "\n");
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
		
//		queryId = IOUtils.readStringInByteArr(dis, ELConsts.QUERY_ID_BYTE_LEN);
		ByteArrayString tmpQid = new ByteArrayString();
		tmpQid.fromFileWithByteLen(dis);
		queryId = tmpQid.toString().trim();
//		System.out.println(queryId);
		try {
			numCandidates = dis.readInt();
//			System.out.println(numCandidates);
			mids = new ByteArrayString[numCandidates];
			aliasLikelihoods = new float[numCandidates];
			popularities = new float[numCandidates];
			npses = new float[numCandidates];
			tfidfSimilarities = new double[numCandidates];
			probabilities = new float[numCandidates];
			wordHitRates = new float[numCandidates];
//			evScores = new double[numCandidates];
			for (int i = 0; i < numCandidates; ++i) {
				mids[i] = new ByteArrayString();
				mids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
				aliasLikelihoods[i] = dis.readFloat();
				popularities[i] = dis.readFloat();
				npses[i] = dis.readFloat();
				tfidfSimilarities[i] = dis.readDouble();
				probabilities[i] = dis.readFloat();
				wordHitRates[i] = dis.readFloat();
//				evScores[i] = dis.readDouble();
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
	public float[] npses = null;
	public double[] tfidfSimilarities = null;
	public float[] probabilities = null;
	public float[] wordHitRates = null;
//	public double[] evScores = null;
}
