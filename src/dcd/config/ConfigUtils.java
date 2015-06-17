package dcd.config;

import dcd.el.dict.AliasDictWithIndex;
import dcd.el.feature.FeatureLoader;
import dcd.el.feature.TfIdfExtractor;
import dcd.el.tac.MidToEidMapper;

public class ConfigUtils {
	public static AliasDictWithIndex getAliasDict(IniFile.Section sect) {
		if (sect == null)
			return null;
		
		String dictAliasFileName = sect.getValue("alias_file"),
				dictAliasIndexFileName = sect.getValue("alias_index_file"),
				dictMidFileName = sect.getValue("mid_file");

		return new AliasDictWithIndex(dictAliasFileName,
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
