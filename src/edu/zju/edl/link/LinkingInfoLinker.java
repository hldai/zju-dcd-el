package edu.zju.edl.link;

import edu.zju.edl.obj.LinkingResult;
import edu.zju.edl.feature.LinkingInfoDoc;
import edu.zju.edl.obj.Mention;

public interface LinkingInfoLinker {
	public LinkingResult[] link(LinkingInfoDoc linkingInfoDoc);
	public LinkingResult[] link14(LinkingInfoDoc linkingInfoDoc);
}
