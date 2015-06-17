// author: DHL brnpoem@gmail.com

package dcd.el.dict;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;

import dcd.el.ELConsts;
import dcd.el.io.IOUtils;
import dcd.el.utils.CommonUtils;

public class AliasDictWithIndex implements AliasDict {
	public class MidBlock {
		public int begPos = 0;
		public int len = 0;
	}
	
	public AliasDictWithIndex(String aliasFileName, String aliasIndexFileName,
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
	
	public LinkedList<String> getMids(String alias) {
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
		
		LinkedList<String> results = getMidsFromMidFile(mb.begPos, mb.len);
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
	
	private LinkedList<String> getMidsFromMidFile(int begPos, int len) {
		LinkedList<String> mids = new LinkedList<String>();
		byte[] bytes = new byte[ELConsts.MID_BYTE_LEN];
		try {
			midRaf.seek(begPos * ELConsts.MID_BYTE_LEN);
			String midFixLen = null;
			for (int i = 0; i < len; ++i) {
				midRaf.read(bytes);
//				System.out.println(new String(bytes));
				midFixLen = new String(bytes);
				mids.add(midFixLen.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return mids;
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
