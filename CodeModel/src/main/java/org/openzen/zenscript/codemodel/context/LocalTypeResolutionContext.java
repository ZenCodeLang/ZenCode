/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalTypeResolutionContext implements TypeResolutionContext {
	private final TypeResolutionContext outer;
	private final CompilingType type;
	private final TypeParameter[] parameters;
	
	public LocalTypeResolutionContext(TypeResolutionContext outer, CompilingType type, TypeParameter[] parameters) {
		this.outer = outer;
		this.type = type;
		this.parameters = parameters;
	}
	
	@Override
	public ZSPackage getRootPackage() {
		return outer.getRootPackage();
	}

	@Override
	public GlobalTypeRegistry getTypeRegistry() {
		return outer.getTypeRegistry();
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return outer.getAnnotation(name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		if (type != null) {
			CompilingType compiling = type.getInner(name.get(0).name);
			if (compiling != null) {
				DefinitionTypeID outer = getTypeRegistry().getForMyDefinition(type.load());
				return compiling.getInnerType(getTypeRegistry(), name, 0, outer);
			}
		}
		
		if (name.size() == 1 && name.get(0).hasNoArguments()) {
			for (TypeParameter parameter : parameters)
				if (parameter.name.equals(name.get(0).name))
					return getTypeRegistry().getGeneric(parameter);
		}
		
		return outer.getType(position, name);
	}
	
	@Override
	public TypeID getThisType() {
		if (type == null)
			return null;
		
		return getTypeRegistry().getForMyDefinition(type.load());
	}
}
