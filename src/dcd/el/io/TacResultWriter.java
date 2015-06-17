package dcd.el.io;

import java.io.BufferedWriter;
import java.io.IOException;

import dcd.el.tac.Result;

public class TacResultWriter {
	public void open(String fileName) {
		writer = IOUtils.getUTF8BufWriter(fileName);
	}
	
	public void writeResult(Result r) {
		try {
			writer.write(r.queryId + "\t" + r.entityId + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private BufferedWriter writer = null;
}
