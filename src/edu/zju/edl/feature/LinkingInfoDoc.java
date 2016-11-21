package edu.zju.edl.feature;

import edu.zju.edl.utils.IOUtils;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by dhl on 8/10/2016.
 * Linking info for a documentl
 */
public class LinkingInfoDoc {
    public void toFile(DataOutputStream dos) {
        IOUtils.writeStringVaryLen(dos, docId);
        try {
            if (linkingInfoMentions == null) {
                dos.writeInt(0);
                return ;
            }

            dos.writeInt(linkingInfoMentions.length);
            for (int i = 0; i < linkingInfoMentions.length; ++i) {
                dos.writeBoolean(isNested[i]);
            }
            for (int i = 0; i < linkingInfoMentions.length; ++i) {
                dos.writeInt(corefChain[i]);
            }
            for (LinkingInfoMention linkingBasisMention : linkingInfoMentions) {
                linkingBasisMention.toFile(dos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toFileVecTrain(DataOutputStream dos) {
        IOUtils.writeStringVaryLen(dos, docId);
        try {
            for (float v : docVec) {
                dos.writeFloat(v);
            }

            if (linkingInfoMentions == null) {
                dos.writeInt(0);
                return ;
            }

            dos.writeInt(linkingInfoMentions.length);
            for (LinkingInfoMention linkingBasisMention : linkingInfoMentions) {
                linkingBasisMention.toFileVecTrain(dos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toFile(BufferedWriter writer) {
        try {
            writer.write(docId + "\n");
            if (linkingInfoMentions == null) {
                return ;
            }
            for (LinkingInfoMention linkingBasisMention : linkingInfoMentions) {
                linkingBasisMention.toFile(writer);
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

        try {
            docId = IOUtils.readStringVaryLen(dis);
            int numMentions = dis.readInt();
//			System.out.println(docId.toString() + "\t" + numMentions);
            linkingInfoMentions = new LinkingInfoMention[numMentions];
            isNested = new boolean[numMentions];
            corefChain = new int[numMentions];
            for (int i = 0; i < numMentions; ++i) {
                isNested[i] = dis.readBoolean();
            }
            for (int i = 0; i < numMentions; ++i) {
                corefChain[i] = dis.readInt();
            }
            for (int i = 0; i < numMentions; ++i) {
                linkingInfoMentions[i] = new LinkingInfoMention();
                linkingInfoMentions[i].fromFile(dis);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String docId = null;
    public LinkingInfoMention[] linkingInfoMentions = null;
    public boolean[] isNested = null;

    public int[] corefChain = null;

    public float[] docVec = null;
}
