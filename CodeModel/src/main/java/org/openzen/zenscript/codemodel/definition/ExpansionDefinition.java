/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpansionDefinition extends HighLevelDefinition {
	public ITypeID target;
	
	public ExpansionDefinition(CodePosition position, ZSPackage pkg, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, pkg, null, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitExpansion(this);
	}
}
