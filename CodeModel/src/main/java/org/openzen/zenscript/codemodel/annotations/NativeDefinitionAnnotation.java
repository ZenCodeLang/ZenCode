/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.scope.BaseScope;

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
	public void apply(HighLevelDefinition definition, BaseScope scope) {
		definition.setTag(NativeTag.class, new NativeTag(identifier));
	}

	@Override
	public void applyOnSubtype(HighLevelDefinition definition, BaseScope scope) {
		// this annotation is not inherited
	}
}
