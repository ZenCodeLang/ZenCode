package org.openzen.zenscript.codemodel;

import org.openzen.zenscript.codemodel.type.TypeID;

public class LocalVariable {
	public final String name;
	public final boolean isFinal;
	public final TypeID type;

	public LocalVariable(String name, boolean isFinal, TypeID type) {
		this.name = name;
		this.isFinal = isFinal;
		this.type = type;
	}
}
