// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import dcd.el.objects.LinkingResult;
import dcd.el.tac.LinkingBasisDoc;

public interface SimpleLinker {
	public LinkingResult[] link(LinkingBasisDoc linkingBasisDoc);
	public LinkingResult[] link14(LinkingBasisDoc linkingBasisDoc);
}
