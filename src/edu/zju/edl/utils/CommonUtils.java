// author: DHL brnpoem@gmail.com

package edu.zju.edl.utils;

public class CommonUtils {
	// a line: 
	// <subject>	<predicate>	<object>	.
	// does not necessarily work for all kind of files 
	public static String getFieldFromLine(String line, int fieldIdx) {
		int begPos = 0, endPos = 0;
		
		for (int i = 0; i < fieldIdx; ++i) {
			begPos = nextTabPos(line, begPos);

			if (begPos < 0)
				return null;
			
			++begPos;
		}
		
		endPos = nextTabPos(line, begPos);
		return line.substring(begPos, endPos);
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

	// TODO switch params
	public static boolean isAbbr(String mainStr, String abbr) {
		boolean isAllUpperCase = true;
		for (int i = 0; isAllUpperCase && i < abbr.length(); ++i) {
			if (!Character.isUpperCase(abbr.charAt(i))) {
				isAllUpperCase = false;
				return false;
			}
		}
		
		int pos = 0;
		for (int i = 0; i < abbr.length(); ++i) {
			char ch = abbr.charAt(i);
			while (pos < mainStr.length()) {
				if (pos == 0 || mainStr.charAt(pos - 1) == ' ') {
					if (mainStr.charAt(pos) == ch) {
						break;
					}
				}
				++pos;
			}
			
			if (pos == mainStr.length())
				return false;
		}

		while (pos < mainStr.length()) {
			if (mainStr.charAt(pos) == ' ')
				return false;
			++pos;
		}

		return true;
	}
	
	// TODO more accurate
	public static boolean hasWord(String text, String word) {
		int fromIdx = 0, wordLen = word.length();
		int idx = 0;
		while (fromIdx < text.length() && (idx = text.indexOf(word, fromIdx)) >= 0) {
			if (isWord(text, idx, idx + wordLen - 1))
				return true;
			fromIdx = idx + 1;
		}
		
		return false;
	}
	
	public static boolean isWord(String text, int idxLeft, int idxRight) {
		boolean leftCool = idxLeft == 0 || charIsBreak(text.charAt(idxLeft - 1));
		boolean rightCool = idxRight == text.length() - 1 || charIsBreak(text.charAt(idxRight + 1));
		return leftCool && rightCool;
	}
	
	public static boolean charIsBreak(char ch) {
		return ch > 0 && ch <= 128 && !((ch <= 'z' && ch >= 'a') || (ch <= 'Z' && ch >= 'A'));
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
