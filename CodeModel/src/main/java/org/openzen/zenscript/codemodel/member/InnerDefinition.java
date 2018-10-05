/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Collections;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeArgument;

/**
 *
 * @author Hoofdgebruiker
 */
public class InnerDefinition {
	public final HighLevelDefinition definition;
	public final Map<TypeParameter, TypeArgument> outerTypeArguments;
	
	public InnerDefinition(HighLevelDefinition definition) {
		this.definition = definition;
		this.outerTypeArguments = Collections.emptyMap();
	}
	
	public InnerDefinition(HighLevelDefinition definition, Map<TypeParameter, TypeArgument> outerTypeArguments) {
		this.definition = definition;
		this.outerTypeArguments = outerTypeArguments;
	}
	
	public DefinitionTypeID instance(GlobalTypeRegistry registry, TypeArgument[] typeArguments, DefinitionTypeID outer) {
		return registry.getForDefinition(definition, typeArguments, outer);
	}
}
