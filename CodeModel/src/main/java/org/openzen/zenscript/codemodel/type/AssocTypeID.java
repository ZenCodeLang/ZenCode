package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.List;
import java.util.Optional;

public class AssocTypeID implements TypeID {
	public final TypeID keyType;
	public final TypeID valueType;
	private final AssocTypeID normalized;

	public AssocTypeID(GlobalTypeRegistry typeRegistry, TypeID keyType, TypeID valueType) {
		this.keyType = keyType;
		this.valueType = valueType;

		if (keyType != keyType.getNormalized() || valueType != valueType.getNormalized())
			normalized = typeRegistry.getAssociative(keyType.getNormalized(), valueType.getNormalized());
		else
			normalized = this;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return mapper.registry.getAssociative(
				keyType.instance(mapper),
				valueType.instance(mapper));
	}

	@Override
	public AssocTypeID getNormalized() {
		return normalized;
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitAssoc(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitAssoc(context, this);
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isValueType() {
		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public Optional<AssocTypeID> asAssoc() {
		return Optional.of(this);
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		keyType.extractTypeParameters(typeParameters);
		valueType.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + keyType.hashCode();
		hash = 29 * hash + valueType.hashCode();
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
		final AssocTypeID other = (AssocTypeID) obj;
		return this.keyType.equals(other.keyType)
				&& this.valueType.equals(other.valueType);
	}

	@Override
	public String toString() {
		return valueType.toString() + '[' + keyType.toString() + ']';
	}
}
