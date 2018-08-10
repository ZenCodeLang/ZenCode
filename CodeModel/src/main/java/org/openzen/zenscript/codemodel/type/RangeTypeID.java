/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class RangeTypeID implements ITypeID {
	public static final RangeTypeID INT = new RangeTypeID(null, BasicTypeID.INT, BasicTypeID.INT);
	
	public final ITypeID from;
	public final ITypeID to;
	private final RangeTypeID normalized;
	
	public RangeTypeID(GlobalTypeRegistry registry, ITypeID from, ITypeID to) {
		this.from = from;
		this.to = to;
		
		if (from.getNormalized() == from && to.getNormalized() == to) {
			normalized = this;
		} else {
			normalized = registry.getRange(from.getNormalized(), to.getNormalized());
		}
	}
	
	@Override
	public RangeTypeID getNormalized() {
		return normalized;
	}
	
	@Override
	public ITypeID instance(GenericMapper mapper) {
		return mapper.registry.getRange(
				from.instance(mapper),
				to.instance(mapper));
	}

	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitRange(this);
	}
	
	@Override
	public RangeTypeID getUnmodified() {
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
		return false;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return from.hasInferenceBlockingTypeParameters(parameters) || to.hasInferenceBlockingTypeParameters(parameters);
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		from.extractTypeParameters(typeParameters);
		to.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + from.hashCode();
		hash = 89 * hash + to.hashCode();
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
		final RangeTypeID other = (RangeTypeID) obj;
		return this.from == other.from && this.to == other.to;
	}
	
	@Override
	public String toString() {
		return from.toString() + " .. " + to.toString();
	}
}
