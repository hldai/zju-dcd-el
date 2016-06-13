// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.config;

import edu.zju.dcd.edl.cg.AliasDictWithIndex;
import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.cg.IndexedAliasDictWithPse;
import edu.zju.dcd.edl.feature.FeatureLoader;
import edu.zju.dcd.edl.feature.TfIdfExtractor;
import edu.zju.dcd.edl.tac.MidToEidMapper;
import edu.zju.dcd.edl.utils.WidMidMapper;

public class ConfigUtils {
	
	public static WidMidMapper getMidWidMapper(IniFile.Section sect) {
		String midWidFileName = sect.getValue("mid_wid_file");
		return new WidMidMapper(midWidFileName);
	}
	
	public static CandidatesRetriever getCandidateRetriever(IniFile.Section sect, MidToEidMapper mteMapper) {
		if (sect == null)
			return null;
		
		IndexedAliasDictWithPse indexedAliasDictWithPse = ConfigUtils.getAliasDictWithPse(sect);
		String midPopularityFileName = sect.getValue("mid_popularity_file"),
				personListFileName = sect.getValue("person_list_file"),
				gpeAdjListFileName = sect.getValue("gpe_adj_list_file");
		return new CandidatesRetriever(indexedAliasDictWithPse, midPopularityFileName, personListFileName,
				gpeAdjListFileName, mteMapper);
	}
	
	public static AliasDictWithIndex getAliasDict(IniFile.Section sect) {
		if (sect == null)
			return null;
		
		String dictAliasFileName = sect.getValue("alias_file"),
				dictAliasIndexFileName = sect.getValue("alias_index_file"),
				dictMidFileName = sect.getValue("mid_file");

		return new AliasDictWithIndex(dictAliasFileName,
				dictAliasIndexFileName, dictMidFileName);
	}
	
	public static IndexedAliasDictWithPse getAliasDictWithPse(IniFile.Section sect) {
		if (sect == null)
			return null;
		
		String dictAliasFileName = sect.getValue("alias_file"),
				dictAliasIndexFileName = sect.getValue("alias_index_file"),
				dictMidFileName = sect.getValue("mid_file");

		return new IndexedAliasDictWithPse(dictAliasFileName,
				dictAliasIndexFileName, dictMidFileName);
	}
	
	public static MidToEidMapper getMidToEidMapper(IniFile.Section sect) {
		if (sect == null)
			return null;
		
		String midToEidFileName = sect.getValue("mid_to_eid_file");
		return new MidToEidMapper(midToEidFileName);
	}
	
	public static FeatureLoader getFeatureLoader(IniFile.Section sect) {
		if (sect == null)
			return null;
		
		String featureFileName = sect.getValue("feature_file"),
				featureIdxFileName = sect.getValue("feature_index_file");
		return new FeatureLoader(featureFileName, featureIdxFileName);
	}
	
	public static TfIdfExtractor getTfIdfExtractor(IniFile.Section sect) {
		if (sect == null)
			return null;
		
		String idfFileName = sect.getValue("idf_file");
		return new TfIdfExtractor(idfFileName);
	}
}
