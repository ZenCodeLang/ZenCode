/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Arrays;
import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionTypeID implements TypeID {
	public final FunctionHeader header;
	private final FunctionTypeID normalized;
	
	public FunctionTypeID(GlobalTypeRegistry registry, FunctionHeader header) {
		this.header = header;
		
		FunctionHeader normalizedHeader = header.normalize(registry);
		normalized = header == normalizedHeader ? this : registry.getFunction(normalizedHeader);
	}
	
	@Override
	public FunctionTypeID getNormalizedUnstored() {
		return normalized;
	}
	
	@Override
	public TypeID instanceUnstored(GenericMapper mapper) {
		return mapper.registry.getFunction(mapper.map(header));
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitFunction(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitFunction(context, this);
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isConst() {
		return false;
	}
	
	@Override
	public boolean isDestructible() {
		return false;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return header.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		header.getReturnType().type.extractTypeParameters(typeParameters);
		for (FunctionParameter parameter : header.parameters)
			parameter.type.type.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 71 * hash + header.getReturnType().hashCode();
		hash = 71 * hash + Arrays.deepHashCode(header.parameters);
		hash = 71 * hash + Arrays.deepHashCode(header.typeParameters);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FunctionTypeID other = (FunctionTypeID) obj;
		return this.header.getReturnType().equals(other.header.getReturnType())
				&& Arrays.deepEquals(this.header.parameters, other.header.parameters)
				&& Arrays.deepEquals(this.header.typeParameters, other.header.typeParameters);
	}
	
	@Override
	public String toString() {
		return toString(null);
	}
	
	@Override
	public String toString(StorageTag storage) {
		StringBuilder result = new StringBuilder();
		result.append("function");
		if (header.typeParameters.length > 0) {
			result.append('<');
			for (int i = 0; i < header.typeParameters.length; i++) {
				if (i > 0)
					result.append(", ");
				
				result.append(header.typeParameters[i].toString());
			}
			result.append('>');
		}
		if (storage != null && storage != ValueStorageTag.INSTANCE) {
			result.append('`');
			result.append(storage);
		}
		result.append('(');
		for (int i = 0; i < header.parameters.length; i++) {
			if (i > 0)
				result.append(", ");

			FunctionParameter parameter = header.parameters[i];
			result.append(parameter.name);
			result.append(" as ");
			result.append(parameter.type);
		}
		result.append(')');
		result.append(" as ");
		result.append(header.getReturnType());
		return result.toString();
	}
}
