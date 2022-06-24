package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.TypeID;

public class TypeSerializationContext {
	private final TypeSerializationContext parent;
	public final TypeID thisType;

	public TypeSerializationContext(TypeSerializationContext parent, TypeID thisType) {
		this.parent = parent;
		this.thisType = thisType;
	}

	public int getId(TypeParameter typeParameter) {

	}
}
