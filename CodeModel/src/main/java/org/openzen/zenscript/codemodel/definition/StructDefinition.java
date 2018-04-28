/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import org.openzen.zenscript.codemodel.HighLevelDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class StructDefinition extends HighLevelDefinition {
	public StructDefinition(ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(pkg, name, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitStruct(this);
	}
}
