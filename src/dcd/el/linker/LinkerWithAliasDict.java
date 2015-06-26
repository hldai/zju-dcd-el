// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import dcd.el.dict.AliasDict;
import dcd.el.dict.CandidatesRetriever;
import dcd.el.objects.Document;
import dcd.el.objects.LinkingResult;
import dcd.el.tac.MidToEidMapper;

public abstract class LinkerWithAliasDict implements EntityLinker {
	// return the mid of the entity linked
	public abstract LinkingResult[] link(Document doc);
	// return the eid of the entity linked
	public abstract LinkingResult[] link14(Document doc);
		
	public LinkerWithAliasDict(AliasDict dict, MidToEidMapper mapper) {
//		this.aliasDict = dict;
		candidatesRetriever = new CandidatesRetriever(dict);
		this.mteMapper = mapper;
	}
	
	
//	protected AliasDict aliasDict = null;
	protected CandidatesRetriever candidatesRetriever = null;
	protected MidToEidMapper mteMapper = null;
}
