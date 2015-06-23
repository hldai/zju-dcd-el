// author: DHL brnpoem@gmail.com

package dcd.el.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import dcd.el.io.IOUtils;

public class Document {
	public boolean loadText(String srcDocPath) {
		if (srcDocPath == null)
			return false;
		
		Path path = Paths.get(srcDocPath, docId + ".xml");
		BufferedReader reader = IOUtils.getUTF8BufReader(path.toString());
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
