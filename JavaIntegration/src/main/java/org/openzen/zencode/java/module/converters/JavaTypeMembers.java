package org.openzen.zencode.java.module.converters;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.type.TypeID;

public class JavaTypeMembers implements ResolvedType {
	private final Class<?> cls;
	private final TypeID[] typeArguments;

	public JavaTypeMembers(Class<?> cls, TypeID[] typeArguments) {
		this.cls = cls;
		this.typeArguments = typeArguments;
	}
}
