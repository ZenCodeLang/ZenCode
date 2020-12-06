package org.openzen.zenscript.codemodel.context;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public interface TypeResolutionContext {
	ZSPackage getRootPackage();

	GlobalTypeRegistry getTypeRegistry();

	AnnotationDefinition getAnnotation(String name);

	TypeID getType(CodePosition position, List<GenericName> name);

	TypeID getThisType();
}
