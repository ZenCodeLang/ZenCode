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

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionTypeID implements ITypeID {
	public final FunctionHeader header;
	private final FunctionTypeID normalized;
	
	public FunctionTypeID(GlobalTypeRegistry registry, FunctionHeader header) {
		this.header = header;
		
		FunctionHeader normalizedHeader = header.normalize(registry);
		normalized = header == normalizedHeader ? this : registry.getFunction(normalizedHeader);
	}
	
	@Override
	public FunctionTypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public ITypeID instance(GenericMapper mapper) {
		return mapper.registry.getFunction(mapper.map(header));
	}
	
	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitFunction(this);
	}
	
	@Override
	public FunctionTypeID getUnmodified() {
		return this;
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
	public boolean isObjectType() {
		return true;
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
		header.getReturnType().extractTypeParameters(typeParameters);
		for (FunctionParameter parameter : header.parameters)
			parameter.type.extractTypeParameters(typeParameters);
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
		return this.header.getReturnType() == other.header.getReturnType()
				&& Arrays.deepEquals(this.header.parameters, other.header.parameters)
				&& Arrays.deepEquals(this.header.typeParameters, other.header.typeParameters);
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
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
