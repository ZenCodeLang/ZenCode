/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class VariantDefinition extends HighLevelDefinition {
	public final List<Option> options = new ArrayList<>();
	
	public VariantDefinition(CodePosition position, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, pkg, name, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitVariant(this);
	}
	
	public static class Option {
		public final String name;
		public final ITypeID[] types;
		
		public Option(String name, ITypeID[] types) {
			this.name = name;
			this.types = types;
		}
		
		public Option instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
			ITypeID[] newTypes = new ITypeID[types.length];
			for (int i = 0; i < types.length; i++)
				newTypes[i] = types[i].withGenericArguments(registry, mapping);
			
			return new Option(name, newTypes);
		}
	}
}
