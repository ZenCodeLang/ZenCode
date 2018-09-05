/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class AliasDefinition extends HighLevelDefinition {
	public ITypeID type;
	
	public AliasDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}
	
	public void setType(ITypeID type) {
		if (type == null)
			throw new NullPointerException("type cannot be null!");
		
		this.type = type;
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitAlias(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitAlias(context, this);
	}

	@Override
	public void normalize(TypeScope scope) {
		// nothing to do
	}
}
