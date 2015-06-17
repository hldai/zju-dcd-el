// author: DHL brnpoem@gmail.com

package dcd.el.utils;

public class CommonUtils {
	// a line: 
	// <subject>	<predicate>	<object>	.
	// does not necessarily work for all kind of files 
	public static String getFieldFromLine(String sline, int fieldIdx) {
		int begPos = 0, endPos = 0;
		
		for (int i = 0; i < fieldIdx; ++i) {
			begPos = nextTabPos(sline, begPos);

			if (begPos < 0)
				return null;
			
			++begPos;
		}
		
		endPos = nextTabPos(sline, begPos);
		
//		if (endPos > sline.length() || begPos > sline.length()) {
//			System.out.println(endPos + " " + begPos + " " + sline.length());
//			System.out.println(sline);
//		}
		
		return sline.substring(begPos, endPos);
	}
	
	public static int countLines(String str) {
		if (str == null) return 0;
		int cnt = 0;
		int len = str.length();
		for (int pos = 0; pos < len; ++pos) {
			char ch = str.charAt(pos);
			if (ch == '\n') {
				++cnt;
			}
		}
		
		return cnt;
	}

//	public static void stringToByteArr(String s, byte[] bytes) {
//		Arrays.fill(bytes, (byte) 0);
//
//		byte[] tmp = s.getBytes();
//		for (int i = 0; i < tmp.length; ++i) {
//			bytes[i] = tmp[i];
//		}
//	}
	
	private static int nextTabPos(String sline, int begPos) {
		while (begPos < sline.length() && (sline.charAt(begPos) != '\t')) {
			++begPos;
		}
		
		return begPos;
	}
}
