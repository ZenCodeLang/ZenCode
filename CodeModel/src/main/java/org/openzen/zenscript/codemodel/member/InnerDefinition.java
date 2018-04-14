/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class InnerDefinition {
	public final HighLevelDefinition definition;
	public final Map<TypeParameter, ITypeID> outerTypeArguments;
	
	public InnerDefinition(HighLevelDefinition definition) {
		this.definition = definition;
		this.outerTypeArguments = Collections.emptyMap();
	}
	
	public InnerDefinition(HighLevelDefinition definition, Map<TypeParameter, ITypeID> outerTypeArguments) {
		this.definition = definition;
		this.outerTypeArguments = outerTypeArguments;
	}
	
	public DefinitionTypeID instance(GlobalTypeRegistry registry, List<ITypeID> typeArguments) {
		return registry.getForDefinition(definition, typeArguments, outerTypeArguments);
	}
}
