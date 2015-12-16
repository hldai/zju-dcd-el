// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.cg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.utils.CommonUtils;

public class IndexedAliasDictWithPse {
	public static class MidPseList {
		public LinkedList<ByteArrayString> mids = null;
		public LinkedList<Float> pses = null;
		public LinkedList<Float> npses = null;
	}
	
	public class MidBlock {
		public int begPos = 0;
		public int len = 0;
	}
	
	public IndexedAliasDictWithPse(String aliasFileName, String aliasIndexFileName,
			String midFileName) {
		try {
			aliasRaf = new RandomAccessFile(aliasFileName, "r");
//			aliasIsr = new InputStreamReader(new FileInputStream(aliasRaf.getFD()), "UTF8");

			int numIndices = IOUtils.getNumLinesFor(aliasIndexFileName);
			
			System.out.println("Load alias index file...");
			loadAliasIndexFile(aliasIndexFileName, numIndices);
			System.out.println("Done.");
			
			midRaf = new RandomAccessFile(midFileName, "r");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			if (aliasReader != null) {
				aliasReader.close();
			}
			aliasIsr.close();
			aliasRaf.close();
			midRaf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public MidPseList getMidPses(String alias) {
		long curTime = System.currentTimeMillis();
		
		alias = alias.toLowerCase();
		
		long aliasFileBegPos = 0;
		String endAlias = null;
		
		int pos = Arrays.binarySearch(idxAliases, alias);
		if (pos >= 0) {
			aliasFileBegPos = idxPos[pos];
			if (pos + 1 < idxAliases.length) {
				endAlias = idxAliases[pos + 1];
			}
		} else if (pos == -1) {
			aliasFileBegPos = 0;
			endAlias = idxAliases[0];
		} else {
			aliasFileBegPos = idxPos[-pos - 2];
			if (-pos - 1 < idxAliases.length) {
				endAlias = idxAliases[-pos - 1];
			}
		}
		
		MidBlock mb = getMidBegPosAndLen(alias, aliasFileBegPos, endAlias);
		if (mb == null) {
			return null;
		}
		
		MidPseList results = getMidPsesFromMidPseFile(mb.begPos, mb.len);
//		System.out.println(alias + "             !!!");
//		for (ByteArrayString mid : results.mids) {
//			System.out.println(mid.toString());
//		}
		retTime += System.currentTimeMillis() - curTime;
		return results;
	}
	
	public long getRetrieveTime() {
		return retTime;
	}
	
	private MidBlock getMidBegPosAndLen(String alias, long aliasFileBegPos, String endAlias) {
		try {
			aliasRaf.seek(aliasFileBegPos);
			aliasIsr = new InputStreamReader(new FileInputStream(aliasRaf.getFD()), "UTF8");
			aliasReader = new BufferedReader(aliasIsr);
			
			MidBlock mb = null;
			String curAliasLine = null;
			while ((curAliasLine = aliasReader.readLine()) != null) {
				String[] vals = curAliasLine.split("\t");
				if (endAlias != null && vals[0].equals(endAlias)) {
					break;
				}
				
				if (alias.equals(vals[0])) {
					mb = new MidBlock();
					mb.begPos = Integer.valueOf(vals[1]);
					mb.len = Integer.valueOf(vals[2]);
					break;
				}
			}
			
//			aliasReader.close();
			
			return mb;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private MidPseList getMidPsesFromMidPseFile(int begPos, int len) {
		MidPseList midPseList = new MidPseList();
		midPseList.mids = new LinkedList<ByteArrayString>();
		midPseList.pses = new LinkedList<Float>();
		midPseList.npses = new LinkedList<Float>();
		try {
			midRaf.seek(begPos * ELConsts.MID_WITH_PSE_BYTE_LEN);
			for (int i = 0; i < len; ++i) {
				ByteArrayString mid = new ByteArrayString();
				mid.fromFileWithFixedLen(midRaf, ELConsts.MID_BYTE_LEN);
				float pse = midRaf.readFloat();
				float npse = midRaf.readFloat();
				
				midPseList.mids.add(mid);
				midPseList.pses.add(pse);
				midPseList.npses.add(npse);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return midPseList;
	}
	
	private void loadAliasIndexFile(String aliasIndexFileName, int numIndices) {
		BufferedReader reader = IOUtils.getUTF8BufReader(aliasIndexFileName);
		
		idxAliases = new String[numIndices];
		idxPos = new long[numIndices];
		
		try {
			String line = null;
			int cnt = 0;
			while ((line = reader.readLine()) != null) {
				if (cnt == numIndices && !line.equals("")) {
					System.out.println("The numIndices parameter may be too small!");
				}
				
				idxAliases[cnt] = CommonUtils.getFieldFromLine(line, 0);
				idxPos[cnt] = Long.valueOf(CommonUtils.getFieldFromLine(line, 1));
				++cnt;
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// for the alias dictionary file, e.g. dict_alias.txt
	RandomAccessFile aliasRaf = null;
	InputStreamReader aliasIsr = null;
	BufferedReader aliasReader = null;
	
	RandomAccessFile midRaf = null; // to memory?

	private String[] idxAliases = null;
	private long[] idxPos = null;
	
	private long retTime = 0;
}
