/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ClassDefinition extends HighLevelDefinition {
	public ClassDefinition(String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(name, modifiers, outerDefinition);
	}
	
	public void setSuperclass(ITypeID superclass) {
		this.superType = superclass;
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitClass(this);
	}
}
