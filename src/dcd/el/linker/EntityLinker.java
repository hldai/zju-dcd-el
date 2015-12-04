// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.LinkingResult;


// given a document with mentions, link the mentions to the corresponding entity
public interface EntityLinker {
	// return the mid of the entity linked
	LinkingResult[] link(Document doc);
	// return the eid of the entity linked. For 2014 entity linking
	LinkingResult[] link14(Document doc);
}
