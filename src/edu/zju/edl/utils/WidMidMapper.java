package edu.zju.edl.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

public class WidMidMapper {
	public WidMidMapper(String widMidFileName) {
		int numWids = IOUtils.getNumLinesFor(widMidFileName);
		wids = new int[numWids];
		mids = new String[numWids];
		BufferedReader reader = IOUtils.getUTF8BufReader(widMidFileName);
		try {
			for (int i = 0; i < numWids; ++i) {
				String line = reader.readLine();
				wids[i] = Integer.valueOf(CommonUtils.getFieldFromLine(line, 1));
				mids[i] = CommonUtils.getFieldFromLine(line, 0);
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getMid(int wid) {
		int pos = Arrays.binarySearch(wids, wid);
		if (pos < 0)
			return null;
		return mids[pos];
	}
	
	public int getWid(String mid) {
		int pos = Arrays.binarySearch(mids, mid);
		if (pos < 0)
			return -1;
		return wids[pos];
	}
	
	public int[] wids;
	public String[] mids;
}
