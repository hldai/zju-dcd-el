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
//				dos.writeFloat(aliasLikelihoods[i]);
//				dos.writeFloat(popularities[i]);
				dos.writeFloat(npses[i]);
				dos.writeDouble(tfidfSimilarities[i]);
				dos.writeFloat(wordHitRates[i]);
//				dos.writeFloat(docVecSimilarities[i]);
//				dos.writeFloat(tmpVals[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toFileVecTrain(DataOutputStream dos) {
		ByteArrayString queryIdBas = new ByteArrayString(queryId);
		queryIdBas.toFileWithByteLen(dos);
		try {
			int cnt = 0;
			for (int i = 0; i < numCandidates; ++i)
				if (wikiVecs[i] != null)
					++cnt;
			
			dos.writeInt(cnt);
			
			for (int i = 0; i < numCandidates; ++i) {
				if (wikiVecs[i] != null) {
					mids[i].toFileWithFixedLen(dos, ELConsts.MID_BYTE_LEN);
					dos.writeFloat(npses[i]);
					for (int j = 0; j < wikiVecs[i].length; ++j) {
						dos.writeFloat(wikiVecs[i][j]);
					}
				}
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
				writer.write(wordHitRates[i] + "\t");
				writer.write(docVecSimilarities[i] + "\n");
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
//			aliasLikelihoods = new float[numCandidates];
//			popularities = new float[numCandidates];
			npses = new float[numCandidates];
			tfidfSimilarities = new double[numCandidates];
			wordHitRates = new float[numCandidates];
//			docVecSimilarities = new float[numCandidates];
//			tmpVals = new float[numCandidates];
			for (int i = 0; i < numCandidates; ++i) {
				mids[i] = new ByteArrayString();
				mids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
//				aliasLikelihoods[i] = dis.readFloat();
//				popularities[i] = dis.readFloat();
				npses[i] = dis.readFloat();
				tfidfSimilarities[i] = dis.readDouble();
				wordHitRates[i] = dis.readFloat();
//				docVecSimilarities[i] = dis.readFloat();
//				tmpVals[i] = dis.readFloat();
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
	public float[] wordHitRates = null;
	public float[] docVecSimilarities = null;
	public float[] tmpVals = null;
	
	public float[][] wikiVecs = null;
}
