package org.openzen.zencode.java;

import org.openzen.zenscript.codemodel.type.TypeID;

import java.lang.reflect.AnnotatedType;

public interface JavaRuntimeTypeConverter {
	TypeID getType(TypeVariableContext context, AnnotatedType type);

	TypeID parseType(String type);
}
