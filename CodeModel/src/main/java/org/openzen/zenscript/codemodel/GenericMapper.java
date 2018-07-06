/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericMapper {
	public final GlobalTypeRegistry registry;
	private final Map<TypeParameter, ITypeID> mapping;
	
	public GenericMapper(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		this.registry = registry;
		this.mapping = mapping;
	}
	
	public Map<TypeParameter, ITypeID> getMapping() {
		return mapping;
	}
	
	public ITypeID map(ITypeID original) {
		return mapping.isEmpty() ? original : original.instance(this);
	}
	
	public ITypeID[] map(ITypeID[] original) {
		if (mapping.isEmpty())
			return original;
		
		ITypeID[] mapped = new ITypeID[original.length];
		for (int i = 0; i < original.length; i++)
			mapped[i] = original[i].instance(this);
		return mapped;
	}
	
	public ITypeID map(GenericTypeID type) {
		return mapping.containsKey(type.parameter) ? mapping.get(type.parameter) : type;
	}
	
	public FunctionHeader map(FunctionHeader original) {
		return mapping.isEmpty() ? original : original.withGenericArguments(this);
	}
}
