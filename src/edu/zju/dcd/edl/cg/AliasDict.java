// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.cg;

import java.util.LinkedList;

import edu.zju.dcd.edl.obj.ByteArrayString;

public interface AliasDict {
	// find mids by alias
	public LinkedList<ByteArrayString> getMids(String alias);
}
