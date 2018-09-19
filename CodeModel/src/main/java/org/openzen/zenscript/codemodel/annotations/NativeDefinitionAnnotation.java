/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

/**
 *
 * @author Hoofdgebruiker
 */
public class NativeDefinitionAnnotation implements DefinitionAnnotation {
	private final String identifier;
	
	public NativeDefinitionAnnotation(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return NativeAnnotationDefinition.INSTANCE;
	}
	
	@Override
	public void apply(HighLevelDefinition definition, BaseScope scope) {
		definition.setTag(NativeTag.class, new NativeTag(identifier));
	}

	@Override
	public void applyOnSubtype(HighLevelDefinition definition, BaseScope scope) {
		// this annotation is not inherited
	}

	@Override
	public void serialize(CodeSerializationOutput output, HighLevelDefinition definition, TypeContext context) {
		output.writeString(identifier);
	}
}
