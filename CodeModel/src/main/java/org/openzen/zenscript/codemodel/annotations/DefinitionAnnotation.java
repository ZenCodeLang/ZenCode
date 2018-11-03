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
public interface DefinitionAnnotation {
	DefinitionAnnotation[] NONE = new DefinitionAnnotation[0];
	
	AnnotationDefinition getDefinition();
	
	void apply(HighLevelDefinition definition, BaseScope scope);
	
	void applyOnSubtype(HighLevelDefinition definition, BaseScope scope);
	
	void serialize(CodeSerializationOutput output, HighLevelDefinition definition, TypeContext context);
}
