// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.obj;

import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.tac.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;

public class Mention {
	public static LinkedList<Mention> loadEdlFile(String edlFile) {
		BufferedReader reader = IOUtils.getUTF8BufReader(edlFile);
		LinkedList<Mention> mentions = new LinkedList<Mention>();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split("\t");
				Mention m = new Mention();
				m.mentionId = vals[1];
				m.nameString = vals[2];

				int colonPos = vals[3].indexOf(':'), dashPos = vals[3].indexOf('-');
				m.docId = vals[3].substring(0, colonPos);
				m.beg = Integer.valueOf(vals[3].substring(colonPos + 1, dashPos));
				m.end = Integer.valueOf(vals[3].substring(dashPos + 1));

				m.entityType = vals[5];
				m.mentionType = vals[6];

				mentions.add(m);
//				System.out.println(q.docId);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return mentions;
	}

	public String mentionId = null;
	public String docId = null;
	public String nameString = null;
	public int beg = -1;
	public int end = -1;
	public String kbid = null;
	public String entityType = null;
	public String mentionType = null;
	
	public static class MentionPosComparator implements Comparator<Mention> {
		@Override
		public int compare(Mention m0, Mention m1) {
			if (m0.beg != m1.beg)
				return m0.beg - m1.beg;
			
			return m1.end - m0.end;
		}
	}
}
