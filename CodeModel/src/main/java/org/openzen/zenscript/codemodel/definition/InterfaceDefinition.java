/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class InterfaceDefinition extends HighLevelDefinition {
	private final List<ITypeID> baseInterfaces = new ArrayList<>();
	
	public InterfaceDefinition(String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(name, modifiers, outerDefinition);
	}
	
	public void addBaseInterface(ITypeID baseInterface) {
		baseInterfaces.add(baseInterface);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitInterface(this);
	}
}
