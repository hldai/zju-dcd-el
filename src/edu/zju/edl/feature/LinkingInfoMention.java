package edu.zju.edl.feature;

import edu.zju.edl.ELConsts;
import edu.zju.edl.obj.ByteArrayString;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Created by dhl on 8/10/2016.
// Linking info for a mention, including the candidates, scores of candidates
public class LinkingInfoMention {
    public void toFile(DataOutputStream dos) {
//		IOUtils.writeStringAsByteArr(dos, queryId, ELConsts.QUERY_ID_BYTE_LEN);
        ByteArrayString mentionIdBas = new ByteArrayString(mentionId);
        mentionIdBas.toFileWithByteLen(dos);
        try {
            dos.writeInt(numCandidates);

            for (int i = 0; i < numCandidates; ++i) {
                mids[i].toFileWithFixedLen(dos, ELConsts.MID_BYTE_LEN);
                dos.writeFloat(commonnesses[i]);
                dos.writeDouble(tfidfSimilarities[i]);
                dos.writeFloat(iwhrs[i]);
//				dos.writeFloat(docVecSimilarities[i]);
//				dos.writeFloat(tmpVals[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toFileVecTrain(DataOutputStream dos) {
        ByteArrayString queryIdBas = new ByteArrayString(mentionId);
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
                    dos.writeFloat(commonnesses[i]);
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
            writer.write(mentionId + "\n");
            writer.write(numCandidates + "\n");
            for (int i = 0; i < numCandidates; ++i) {
                writer.write(mids[i].toString().trim() + "\t");
                writer.write(commonnesses[i] + "\t");
                writer.write(tfidfSimilarities[i] + "\t");
                writer.write(iwhrs[i] + "\t");
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

        ByteArrayString tmpMentionId = new ByteArrayString();
        tmpMentionId.fromFileWithByteLen(dis);
        mentionId = tmpMentionId.toString().trim();
//		System.out.println(mentionId);
        try {
            numCandidates = dis.readInt();
//			System.out.println(numCandidates);
            mids = new ByteArrayString[numCandidates];
            commonnesses = new float[numCandidates];
            tfidfSimilarities = new double[numCandidates];
            iwhrs = new float[numCandidates];
//			docVecSimilarities = new float[numCandidates];
//			tmpVals = new float[numCandidates];
            for (int i = 0; i < numCandidates; ++i) {
                mids[i] = new ByteArrayString();
                mids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
                commonnesses[i] = dis.readFloat();
                tfidfSimilarities[i] = dis.readDouble();
                iwhrs[i] = dis.readFloat();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String mentionId = null;
    public int numCandidates = 0;

    public ByteArrayString[] mids = null;
    public float[] commonnesses = null;
    public double[] tfidfSimilarities = null;
    public float[] iwhrs = null;  // important word hit rates

    public float[] docVecSimilarities = null;
    public float[][] wikiVecs = null;
}
