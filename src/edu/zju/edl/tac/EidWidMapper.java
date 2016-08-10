package edu.zju.edl.tac;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

import edu.zju.edl.utils.IOUtils;

public class EidWidMapper {
	public EidWidMapper(String fileName) {
		int numLines = IOUtils.getNumLinesFor(fileName);
		eids = new String[numLines];
		wids = new int[numLines];
		
		BufferedReader reader = IOUtils.getUTF8BufReader(fileName);
		try {
			String line = null;
			
			for (int i = 0; i < numLines; ++i) {
				line = reader.readLine();
				String vals[] = line.split("\t");
				eids[i] = vals[0];
				wids[i] = Integer.valueOf(vals[1]);
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getWid(String eid) {
		int pos = Arrays.binarySearch(eids, eid);
		if (pos < 0)
			return -1;
		return wids[pos];
	}
	
	private String[] eids = null;
	private int[] wids = null;
}
