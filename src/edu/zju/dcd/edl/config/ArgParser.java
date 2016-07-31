//package edu.zju.dcd.edl.config;
//
//import java.util.HashMap;
//
//public class ArgParser {
//	public boolean parse(String args[]) {
//		keyValues = new HashMap<String, String>();
//
//		int idx = 0;
//		while (idx < args.length) {
//			String key = args[idx];
//			if (idx + 1 == args.length)
//				return false;
//
//			String value = args[idx + 1];
////			System.out.println(value + "\t" + key);
//			keyValues.put(key, value);
//			idx += 2;
//		}
//
//		return true;
//	}
//
//	public String getValue(String arg) {
//		return keyValues.get(arg);
//	}
//
//	private HashMap<String, String> keyValues = null;
//}
