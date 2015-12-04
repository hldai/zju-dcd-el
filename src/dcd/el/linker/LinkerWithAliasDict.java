// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import edu.zju.dcd.edl.cg.AliasDict;
import edu.zju.dcd.edl.cg.CandidatesRetriever;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.tac.MidToEidMapper;

public abstract class LinkerWithAliasDict implements EntityLinker {
	// return the mid of the entity linked
	public abstract LinkingResult[] link(Document doc);
	// return the eid of the entity linked
	public abstract LinkingResult[] link14(Document doc);
		
	public LinkerWithAliasDict(AliasDict dict, MidToEidMapper mapper) {
//		this.aliasDict = dict;
		// TODO
//		candidatesRetriever = new CandidatesRetriever(dict);
		this.mteMapper = mapper;
	}
	
	
//	protected AliasDict aliasDict = null;
	protected CandidatesRetriever candidatesRetriever = null;
	protected MidToEidMapper mteMapper = null;
}
