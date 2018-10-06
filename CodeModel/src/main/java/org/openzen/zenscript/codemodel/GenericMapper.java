/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StoredType;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericMapper {
	public static final GenericMapper EMPTY = new GenericMapper(null, Collections.emptyMap());
	
	public final GlobalTypeRegistry registry;
	private final Map<TypeParameter, StoredType> mapping;
	
	public GenericMapper(GlobalTypeRegistry registry, Map<TypeParameter, StoredType> mapping) {
		this.registry = registry;
		this.mapping = mapping;
	}
	
	public Map<TypeParameter, StoredType> getMapping() {
		return mapping;
	}
	
	public StoredType map(CodePosition position, StoredType original) {
		return mapping.isEmpty() ? original : original.instance(this);
	}
	
	public StoredType[] map(StoredType[] original) {
		if (mapping.isEmpty() || original.length == 0)
			return original;
		
		StoredType[] mapped = new StoredType[original.length];
		for (int i = 0; i < original.length; i++)
			mapped[i] = original[i].instance(this);
		return mapped;
	}
	
	public StoredType map(GenericTypeID type) {
		//if (!mapping.containsKey(type.parameter))
		//	throw new IllegalStateException("No mapping found for type " + type);
		
		return mapping.containsKey(type.parameter) ? mapping.get(type.parameter) : type.stored();
	}
	
	public FunctionHeader map(FunctionHeader original) {
		return mapping.isEmpty() ? original : original.withGenericArguments(registry, this);
	}
	
	public GenericMapper getInner(GlobalTypeRegistry registry, Map<TypeParameter, StoredType> mapping) {
		Map<TypeParameter, StoredType> resultMap = new HashMap<>(this.mapping);
		resultMap.putAll(mapping);
		return new GenericMapper(registry, resultMap);
	}
	
	public GenericMapper getInner(GlobalTypeRegistry registry, TypeParameter[] parameters) {
		Map<TypeParameter, StoredType> resultMap = new HashMap<>(this.mapping);
		for (TypeParameter parameter : parameters)
			resultMap.put(parameter, new StoredType(registry.getGeneric(parameter), null));
		return new GenericMapper(registry, resultMap);
	}
	
	@Override
	public String toString() {
		if (mapping.isEmpty())
			return "{}";
		
		StringBuilder result = new StringBuilder();
		result.append('{');
		boolean first = true;
		for (Map.Entry<TypeParameter, StoredType> entry : mapping.entrySet()) {
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
