package org.openzen.zencode.java.impl.conversion;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.compilation.CompileContext;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.types.ObjectTypeSymbol;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JavaCompileContext extends CompileContext {

	private static final GenericName OBJECT_NAME = new GenericName("Object");
	public JavaCompileContext(ZSPackage rootPackage, ZSPackage modulePackage, List<ExpansionSymbol> expansions, Map<String, IGlobal> globals, List<AnnotationDefinition> annotations) {
		super(rootPackage, modulePackage, expansions, globals, annotations);
	}

	@Override
	public Optional<TypeID> resolve(CodePosition position, List<GenericName> name) {

		if(name.size() == 1) {
			GenericName genericName = name.get(0);
			if(genericName.equals(OBJECT_NAME)) {
				return Optional.of(DefinitionTypeID.create(ObjectTypeSymbol.INSTANCE));
			}
		}
		return super.resolve(position, name);
	}
}
