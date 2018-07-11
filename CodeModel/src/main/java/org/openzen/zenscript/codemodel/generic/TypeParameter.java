/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeParameter {
	public static final TypeParameter[] NONE = new TypeParameter[0];
	
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
	
	public boolean isObjectType() {
		for (GenericParameterBound bound : bounds)
			if (bound.isObjectType())
				return true;
		
		return false;
	}
	
	public boolean matches(LocalMemberCache cache, ITypeID type) {
		for (GenericParameterBound bound : bounds) {
			if (!bound.matches(cache, type))
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
	
	public String getCanonical() {
		StringBuilder result = new StringBuilder();
		result.append(name);
		for(GenericParameterBound bound : bounds) {
			result.append(':');
			result.append(bound.getCanonical());
		}
		return result.toString();
	}
	
	@Override
	public String toString() {
		return name + "@" + position.toShortString();
	}
}
