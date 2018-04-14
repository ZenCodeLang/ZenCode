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
public class EnumDefinition extends HighLevelDefinition {
	public EnumDefinition(String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(name, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitEnum(this);
	}
}
