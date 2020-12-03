package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.Tag;

public class NativeTag implements Tag {
	public final String value;
	
	public NativeTag(String value) {
		this.value = value;
	}
}
