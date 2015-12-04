// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.tac;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.zju.dcd.edl.io.IOUtils;

//the linking basis of a document
//the data needed to link the mentions in a document
public class LinkingBasisDoc {
	public void toFile(DataOutputStream dos) {
		IOUtils.writeStringVaryLen(dos, docId);
		try {
			if (linkingBasisMentions == null) {
				dos.writeInt(0);
				return ;
			}
			
			dos.writeInt(linkingBasisMentions.length);
			for (int i = 0; i < linkingBasisMentions.length; ++i) {
				dos.writeBoolean(isNested[i]);
			}
			for (int i = 0; i < linkingBasisMentions.length; ++i) {
				dos.writeInt(corefChain[i]);
			}
			for (int i = 0; i < linkingBasisMentions.length; ++i) {
				for (int j = 0; j < linkingBasisMentions.length; ++j) {
					dos.writeBoolean(possibleCoref[i][j]);
				}
			}
			for (LinkingBasisMention linkingBasisMention : linkingBasisMentions) {
				linkingBasisMention.toFile(dos);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toFile(BufferedWriter writer) {
		try {
			writer.write(docId + "\n");
			if (linkingBasisMentions == null) {
				return ;
			}
			for (LinkingBasisMention linkingBasisMention : linkingBasisMentions) {
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
			linkingBasisMentions = new LinkingBasisMention[numMentions];
			isNested = new boolean[numMentions];
			corefChain = new int[numMentions];
			possibleCoref = new boolean[numMentions][numMentions];
			for (int i = 0; i < numMentions; ++i) {
				isNested[i] = dis.readBoolean();
			}
			for (int i = 0; i < numMentions; ++i) {
				corefChain[i] = dis.readInt();
			}
			for (int i = 0; i < numMentions; ++i) {
				for (int j = 0; j < numMentions; ++j) {
					possibleCoref[i][j] = dis.readBoolean();
				}
			}
			for (int i = 0; i < numMentions; ++i) {
				linkingBasisMentions[i] = new LinkingBasisMention();
				linkingBasisMentions[i].fromFile(dis);
			}
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public String docId = null;
	public LinkingBasisMention[] linkingBasisMentions = null;
	public boolean[] isNested = null;
	
	public int[] corefChain = null;
	
	public boolean[][] possibleCoref = null;
}
