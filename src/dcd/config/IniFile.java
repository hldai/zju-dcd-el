// author: DHL brnpoem@gmail.com

package dcd.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import dcd.el.io.IOUtils;

public class IniFile {
	public class Section {
		public String getValue(String key) {
			return keyValues.get(key);
		}
		
		public Integer getIntValue(String key) {
			String sval = getValue(key);
			if (sval == null)
				return null;
			
			return Integer.valueOf(getValue(key));
		}
		
		public String name = new String();
		public Map<String, String> keyValues = new HashMap<String, String>();
	}
	
	public IniFile(String fileName) {
		BufferedReader reader = IOUtils.getUTF8BufReader(fileName);
		
		Section section = new Section();
		section.name = new String("NULL");
		
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				
				if (line.length() == 0 || line.charAt(0) == '#')
					continue;
				
				if (line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') {
					sections.add(section); // add previous section
					
					section = new Section();
					section.name = line.substring(1, line.length() - 1);
				}
				
				
				int eqPos = findFirstEqual(line);
				
				if (eqPos < 0)
					continue;
				
				String key = line.substring(0, eqPos).trim();
				String value = line.substring(eqPos + 1, line.length()).trim();
				section.keyValues.put(key, value);
			}
			
			sections.add(section);
			
			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int findFirstEqual(String s) {
		int pos = 0;
		while (pos < s.length() && s.charAt(pos) != '=') ++pos;
		
		if (pos == s.length())
			pos = -1;
		
		return pos;
	}
	
	public Section getSection(String sectionName) {
		for (Section sect : sections) {
			if (sect.name.equals(sectionName)) {
				return sect;
			}
		}
		
		return null;
	}

	public String getValue(String sectionName, String key) {
		Section sect = getSection(sectionName);
		if (sect == null)
			return null;
		
		return sect.getValue(key);
	}
	
	public int getIntValue(String sectionName, String key) {
		return Integer.valueOf(getValue(sectionName, key));
	}
	
	
	private LinkedList<Section> sections = new LinkedList<Section>();
}
