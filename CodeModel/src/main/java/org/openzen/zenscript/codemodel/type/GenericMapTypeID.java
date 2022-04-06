package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GenericMapTypeID implements TypeID {
	public final TypeID value;
	public final TypeParameter key;
	private final GenericMapTypeID normalized;

	public GenericMapTypeID(GlobalTypeRegistry registry, TypeID value, TypeParameter key) {
		this.value = value;
		this.key = key;

		normalized = value.getNormalized() == value ? this : registry.getGenericMap(value.getNormalized(), key);
	}

	@Override
	public GenericMapTypeID getNormalized() {
		return normalized;
	}

	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitGenericMap(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitGenericMap(context, this);
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
	public Optional<GenericMapTypeID> asGenericMap() {
		return Optional.of(this);
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return mapper.registry.getGenericMap(value.instance(mapper), key);
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		value.extractTypeParameters(typeParameters);
		typeParameters.remove(key);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(value.toString());
		result.append("[<");
		result.append(key.toString());
		result.append(">]");
		return result.toString();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + this.value.hashCode();
		hash = 97 * hash + this.key.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final GenericMapTypeID other = (GenericMapTypeID) obj;
		return Objects.equals(this.value, other.value)
				&& Objects.equals(this.key, other.key);
	}
}
