// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import edu.zju.dcd.edl.io.IOUtils;

public class Document {
	private static final String DOC_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";

	public boolean loadText(String srcDocPath) {
		BufferedReader reader = IOUtils.getUTF8BufReader(srcDocPath);
		try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
			
			text = sb.substring(DOC_HEAD.length());
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
