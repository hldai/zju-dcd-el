// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.tac.LinkingBasisDoc;

public interface SimpleLinker {
	public LinkingResult[] link(LinkingBasisDoc linkingBasisDoc);
	public LinkingResult[] link14(LinkingBasisDoc linkingBasisDoc);
}
