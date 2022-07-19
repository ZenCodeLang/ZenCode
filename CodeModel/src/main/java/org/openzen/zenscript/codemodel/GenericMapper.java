package org.openzen.zenscript.codemodel;

import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GenericMapper {

	public static final GenericMapper EMPTY = new GenericMapper(Collections.emptyMap());

	private final Map<TypeParameter, TypeID> mapping;

	public GenericMapper(Map<TypeParameter, TypeID> mapping) {
		if (mapping == null)
			throw new IllegalArgumentException();

		this.mapping = mapping;
	}

	public static GenericMapper create(TypeParameter[] typeParameters, TypeID[] typeArguments) {
		if (typeParameters.length != typeArguments.length)
			throw new IllegalArgumentException("Number of type parameters is different from number of type arguments");

		Map<TypeParameter, TypeID> mapping = new HashMap<>();
		for (int i = 0; i < typeParameters.length; i++) {
			mapping.put(typeParameters[i], typeArguments[i]);
		}
		return new GenericMapper(mapping);
	}

	public static GenericMapper single(TypeParameter parameter, TypeID argument) {
		return new GenericMapper(Collections.singletonMap(parameter, argument));
	}

    public Map<TypeParameter, TypeID> getMapping() {
		return mapping;
	}

	public TypeID map(TypeID original) {
		return mapping.isEmpty() ? original : original.instance(this);
	}

	public TypeID[] map(TypeID[] original) {
		if (mapping.isEmpty() || original.length == 0)
			return original;

		TypeID[] mapped = new TypeID[original.length];
		for (int i = 0; i < original.length; i++)
			mapped[i] = original[i].instance(this);
		return mapped;
	}

	public TypeID mapGeneric(GenericTypeID type) {
		//if (!mapping.containsKey(type.parameter))
		//	throw new IllegalStateException("No mapping found for type " + type);

		return mapping.getOrDefault(type.parameter, type);
	}

	public FunctionHeader map(FunctionHeader original) {
		return mapping.isEmpty() ? original : original.withGenericArguments(this);
	}

	public FieldInstance map(FieldSymbol field) {
		return new FieldInstance(field, map(field.getType()));
	}

	public MethodInstance map(TypeID target, MethodSymbol method) {
		return new MethodInstance(method, map(method.getHeader()), target);
	}

	public GenericMapper getInner(Map<TypeParameter, TypeID> mapping) {
		Map<TypeParameter, TypeID> resultMap = new HashMap<>(this.mapping);
		mapping.forEach((typeParameter, type) -> {
			if (resultMap.containsKey(typeParameter)) {
				if (type.asGeneric().map(generic -> generic.parameter == typeParameter).orElse(false))
					return;
			}
			resultMap.put(typeParameter, type);
		});

		return new GenericMapper(resultMap);
	}

	public GenericMapper getInner(TypeParameter[] parameters) {
		Map<TypeParameter, TypeID> resultMap = new HashMap<>(this.mapping);
		for (TypeParameter parameter : parameters)
			resultMap.put(parameter, new GenericTypeID(parameter));
		return new GenericMapper(resultMap);
	}

	@Override
	public String toString() {
		if (mapping.isEmpty())
			return "{}";

		StringBuilder result = new StringBuilder();
		result.append('{');
		boolean first = true;
		for (Map.Entry<TypeParameter, TypeID> entry : mapping.entrySet()) {
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
