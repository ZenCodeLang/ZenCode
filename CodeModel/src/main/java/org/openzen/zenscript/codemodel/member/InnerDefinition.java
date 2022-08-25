package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.Map;

public class InnerDefinition {
	public final HighLevelDefinition definition;
	public final Map<TypeParameter, TypeID> outerTypeArguments;

	public InnerDefinition(HighLevelDefinition definition) {
		this.definition = definition;
		this.outerTypeArguments = Collections.emptyMap();
	}

	public InnerDefinition(HighLevelDefinition definition, Map<TypeParameter, TypeID> outerTypeArguments) {
		this.definition = definition;
		this.outerTypeArguments = outerTypeArguments;
	}

	public DefinitionTypeID instance(TypeID[] typeArguments, DefinitionTypeID outer) {
		return new DefinitionTypeID(definition, typeArguments, outer);
	}
}
