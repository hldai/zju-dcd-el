// author: DHL brnpoem@gmail.com

package dcd.el.tac;

import java.io.DataInputStream;
import java.io.IOException;

import dcd.el.io.IOUtils;

// TODO change name
public class DocMentionCandidates {
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
			mentionsCandidates = new MentionCandidates[numMentions];
			for (int i = 0; i < numMentions; ++i) {
				mentionsCandidates[i] = new MentionCandidates();
				mentionsCandidates[i].fromFile(dis);
			}
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public String docId = null;
	public MentionCandidates[] mentionsCandidates = null;
}
