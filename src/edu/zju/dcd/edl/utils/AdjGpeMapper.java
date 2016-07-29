package edu.zju.dcd.edl.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import edu.zju.dcd.edl.io.IOUtils;

// TODO remove
public class AdjGpeMapper {
	private static class AdjName implements Comparable<AdjName> {
		public String adj = null;
		public String name = null;
		
		public AdjName() {
		}
		
		public AdjName(String adj, String name) {
			this.adj = adj;
			this.name = name;
		}

		@Override
		public int compareTo(AdjName adjName) {
			return adj.compareTo(adjName.adj);
		}
	}
	
	public AdjGpeMapper(String fileName) {
		System.out.println("loading " + fileName);
		BufferedReader reader = IOUtils.getUTF8BufReader(fileName);
		try {
			LinkedList<AdjName> adjNames = new LinkedList<AdjName>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split("\t");
				
				AdjName adjName = new AdjName();
				adjName.name = vals[0];
				adjName.adj = vals[1];
				adjNames.add(adjName);
			}
			
			reader.close();
			
			this.adjNames = new AdjName[adjNames.size()];
			adjNames.toArray(this.adjNames);
			
			Arrays.sort(this.adjNames);
			System.out.println("done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getName(String adj) {
		AdjName adjName = new AdjName(adj, null);
		int pos = Arrays.binarySearch(adjNames, adjName);
		if (pos < 0)
			return null;
		
		return adjNames[pos].name;
	}
	
	AdjName[] adjNames = null;
}
