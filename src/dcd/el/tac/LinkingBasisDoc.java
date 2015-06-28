// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dcd.el.io.IOUtils;

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
			for (LinkingBasisMention linkingBasisMention : linkingBasisMentions) {
				linkingBasisMention.toFile(dos);
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
			linkingBasisMentions = new LinkingBasisMention[numMentions];
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
}
