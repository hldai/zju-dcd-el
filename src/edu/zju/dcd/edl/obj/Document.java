// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import edu.zju.dcd.edl.io.IOUtils;

public class Document {
	public boolean loadText(String srcDocPath) {
		if (srcDocPath == null)
			return false;
		
		String path = Paths.get(srcDocPath, docId).toString();
		File file = new File(path);
		
		if (!file.exists()) {
			path = Paths.get(srcDocPath, docId + ".xml").toString();
			file = new File(path);
		}
		
		if (!file.exists()) {
			path = Paths.get(srcDocPath, docId + ".df.xml").toString();
			file = new File(path);
		}
		
		if (!file.exists()) {
			path = Paths.get(srcDocPath, docId + ".nw.xml").toString();
		}
		
		BufferedReader reader = IOUtils.getUTF8BufReader(path);
		try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
			
			text = new String(sb);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void releaseText() {
		text = null;
	}
	
	public String text = null;
	public String docId = null;
	public Mention[] mentions = null;
}
