package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class TypeVariableContext {
	@SuppressWarnings("rawtypes")
	private final Map<TypeVariable, TypeParameter> typeVariables = new HashMap<>();

	@SuppressWarnings("rawtypes")
	public void put(TypeVariable variable, TypeParameter parameter) {
		typeVariables.put(variable, parameter);
	}

	@SuppressWarnings("rawtypes")
	public TypeParameter get(TypeVariable variable) {
		if (!typeVariables.containsKey(variable))
			throw new IllegalStateException("Could not find type variable " + variable.getName() + " on declaration: " + variable.getGenericDeclaration().toString());

		return typeVariables.get(variable);
	}

	public void putAllFrom(TypeVariableContext context) {
		typeVariables.putAll(context.typeVariables);
	}
}
