// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import java.util.LinkedList;

import dcd.el.dict.AliasDict;
import dcd.el.objects.Document;
import dcd.el.objects.LinkingResult;
import dcd.el.objects.Mention;
import dcd.el.tac.MidToEidMapper;

public abstract class LinkerWithAliasDict implements EntityLinker {
	// return the mid of the entity linked
	public abstract LinkingResult[] link(Document doc);
	// return the eid of the entity linked
	public abstract LinkingResult[] link14(Document doc);
		
	public LinkerWithAliasDict(AliasDict dict, MidToEidMapper mapper) {
		this.aliasDict = dict;
		this.mteMapper = mapper;
	}
	
	
	protected AliasDict aliasDict = null;
	protected MidToEidMapper mteMapper = null;
}
