/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeParameter {
	public final CodePosition position;
	public final String name;
	public final List<GenericParameterBound> bounds = new ArrayList<>();
	
	public TypeParameter(CodePosition position, String name) {
		this.position = position;
		this.name = name;
	}
	
	private TypeParameter(CodePosition position, String name, List<GenericParameterBound> bounds) {
		this.position = position;
		this.name = name;
		bounds.addAll(bounds);
	}
	
	public void addBound(GenericParameterBound bound) {
		bounds.add(bound);
	}
	
	public boolean matches(ITypeID type) {
		for (GenericParameterBound bound : bounds) {
			if (!bound.matches(type))
				return false;
		}
		
		return true;
	}
	
	public TypeParameter withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		// TODO: think about this...
		//List<GenericParameterBound> bounds = new ArrayList<>();
		//for (GenericParameterBound bound : this.bounds)
		//	bounds.add(bound.withGenericArguments(registry, arguments));
		//return new TypeParameter(name, bounds);
		return this;
	}
	
	public String toString() {
		return name + "[" + position.toShortString() + "]";
	}
}