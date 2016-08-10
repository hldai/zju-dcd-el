package edu.zju.edl.link;

import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.edl.feature.LinkingInfoDoc;

public interface LinkingInfoLinker {
	public LinkingResult[] link(LinkingInfoDoc linkingInfoDoc);
	public LinkingResult[] link14(LinkingInfoDoc linkingInfoDoc);
}
