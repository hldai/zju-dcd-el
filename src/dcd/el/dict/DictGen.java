// author: DHL brnpoem@gmail.com

package dcd.el.dict;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import dcd.el.ELConsts;
import dcd.el.io.IOUtils;
import dcd.el.utils.CommonUtils;

public class DictGen {	
	// dstAliasFile:
	// [alias]
	// [begpos of mids in dstMidFile]
	// [len]
	//
	// dstMidFile: binary file
	// [mid with MID_BYTE_LEN bytes]
	public static void genDict(String midNameFile, String dstAliasFile, String dstMidFile) {
		BufferedReader reader = IOUtils.getUTF8BufReader(midNameFile);
		BufferedWriter nameWriter = IOUtils.getUTF8BufWriter(dstAliasFile);
		
		try {
			BufferedOutputStream midBos = new BufferedOutputStream(new FileOutputStream(dstMidFile));
		
			String line = null;
			String mid = null, name = null, preName = null;
			int cnt = 0, aliasCnt = 0;
			int begPos = 0, len = 0;
			byte[] byteArr = new byte[ELConsts.MID_BYTE_LEN];
		
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					System.out.println("warning: empty line!");
				}
				
				mid = CommonUtils.getFieldFromLine(line, 0);
				name = CommonUtils.getFieldFromLine(line, 1);

				if (preName != null && !name.equals(preName)) {
					nameWriter.write(len + "\n");
				}
				
				if (preName == null || !name.equals(preName)) {
					nameWriter.write(name + "\n" + begPos + "\n");
					++aliasCnt;
					len = 0;
				}

				
				stringToByteArr(mid, byteArr);
				midBos.write(byteArr);
				++begPos;
				++len;
				
				preName = name;
				++cnt;
				
//				if (cnt == 100) break;
			}
			
			nameWriter.write(len + "\n");
			
			System.out.println(cnt + " lines read.");
			System.out.println(aliasCnt + " aliases written.");
			
			reader.close();
			nameWriter.close();
			midBos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getMaxMidLen(String midNameFile) {
		BufferedReader reader = IOUtils.getUTF8BufReader(midNameFile);
		
		int maxLen = -1;
		
		try {
			String line = null;
			String mid = null;
			
			while ((line = reader.readLine()) != null) {
				mid = CommonUtils.getFieldFromLine(line, 0);
				if (mid.length() > maxLen) {
					maxLen = mid.getBytes().length;
				}
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Max mid length: " + maxLen);
		return maxLen;
	}
	
	// remove duplicate rows in ordered file fileName.
	public static void removeDuplicates(String srcFileName, String dstFileName) {
		BufferedReader reader = IOUtils.getUTF8BufReader(srcFileName);
		BufferedWriter writer = IOUtils.getUTF8BufWriter(dstFileName);
		
		try {
			String line = null, preLine = null;
			while ((line = reader.readLine()) != null) {
				if (preLine == null || !line.equals(preLine)) {
					writer.write(line + "\n");
				}
				
				preLine = line;
			}
			
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void stringToByteArr(String s, byte[] bytes) {
		Arrays.fill(bytes, (byte)0);
		
		byte[] tmp = s.getBytes();
		for (int i = 0; i < tmp.length; ++i) {
			bytes[i] = tmp[i];
		}
	}
}
