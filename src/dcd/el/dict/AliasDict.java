// author: DHL brnpoem@gmail.com

package dcd.el.dict;

import java.util.LinkedList;

import dcd.el.objects.ByteArrayString;

public interface AliasDict {
	// find mids by alias
	public LinkedList<ByteArrayString> getMids(String alias);
}
