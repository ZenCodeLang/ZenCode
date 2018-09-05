/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class InterfaceDefinition extends HighLevelDefinition {
	public final List<ITypeID> baseInterfaces = new ArrayList<>();
	
	public InterfaceDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}
	
	public void addBaseInterface(ITypeID baseInterface) {
		baseInterfaces.add(baseInterface);
	}
	
	@Override
	public boolean isDestructible() {
		for (ITypeID baseInterface : baseInterfaces)
			if (baseInterface.isDestructible())
				return true;
		
		return super.isDestructible();
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitInterface(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitInterface(context, this);
	}
}
