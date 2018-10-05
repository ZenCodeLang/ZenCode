/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.TypeArgument;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpansionDefinition extends HighLevelDefinition {
	public TypeArgument target;
	
	public ExpansionDefinition(CodePosition position, Module module, ZSPackage pkg, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, null, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitExpansion(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitExpansion(context, this);
	}
}
