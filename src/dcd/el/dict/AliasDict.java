// author: DHL brnpoem@gmail.com

package dcd.el.dict;

import java.util.LinkedList;

public interface AliasDict {
	// find mids by alias
	public LinkedList<String> getMids(String alias);
}
