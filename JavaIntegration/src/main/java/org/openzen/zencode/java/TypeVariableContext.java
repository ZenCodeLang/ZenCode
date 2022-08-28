package org.openzen.zencode.java;

import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class TypeVariableContext {
	public static final TypeVariableContext EMPTY = new TypeVariableContext();

	private final Map<TypeVariable<?>, TypeParameter> typeVariables = new HashMap<>();
	private final TypeVariableContext parent;

	public TypeVariableContext() {
		this.parent = null;
	}

	private TypeVariableContext(TypeVariableContext parent) {
		this.parent = parent;
	}

	public TypeVariableContext createChildContext() {
		return new TypeVariableContext(this);
	}

	public void put(TypeVariable<?> variable, TypeParameter parameter) {
		typeVariables.put(variable, parameter);
	}

	@SuppressWarnings("rawtypes")
	public TypeParameter get(TypeVariable variable) {
		if (!typeVariables.containsKey(variable)) {
			if (parent != null)
				return parent.get(variable);
			else
				throw new IllegalStateException("Could not find type variable " + variable.getName() + " on declaration: " + variable.getGenericDeclaration().toString());
		}

		return typeVariables.get(variable);
	}
}
