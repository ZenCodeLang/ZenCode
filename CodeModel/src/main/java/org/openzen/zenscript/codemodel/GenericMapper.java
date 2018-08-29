/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.Collections;
import java.util.HashMap;
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
	public static final GenericMapper EMPTY = new GenericMapper(null, Collections.emptyMap());
	
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
		if (!mapping.containsKey(type.parameter))
			throw new IllegalStateException("No mapping found for type " + type);
		
		return mapping.containsKey(type.parameter) ? mapping.get(type.parameter) : type;
	}
	
	public FunctionHeader map(FunctionHeader original) {
		return mapping.isEmpty() ? original : original.withGenericArguments(registry, this);
	}
	
	public GenericMapper getInner(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		Map<TypeParameter, ITypeID> resultMap = new HashMap<>(this.mapping);
		resultMap.putAll(mapping);
		return new GenericMapper(registry, resultMap);
	}
	
	public GenericMapper getInner(GlobalTypeRegistry registry, TypeParameter[] parameters) {
		Map<TypeParameter, ITypeID> resultMap = new HashMap<>(this.mapping);
		for (TypeParameter parameter : parameters)
			resultMap.put(parameter, registry.getGeneric(parameter));
		return new GenericMapper(registry, resultMap);
	}
	
	@Override
	public String toString() {
		if (mapping.isEmpty())
			return "{}";
		
		StringBuilder result = new StringBuilder();
		result.append('{');
		boolean first = true;
		for (Map.Entry<TypeParameter, ITypeID> entry : mapping.entrySet()) {
			if (first) {
				first = false;
			} else {
				result.append(", ");
			}
			result.append(entry.getKey().toString()).append(": ").append(entry.getValue());
		}
		result.append('}');
		return result.toString();
	}
}
