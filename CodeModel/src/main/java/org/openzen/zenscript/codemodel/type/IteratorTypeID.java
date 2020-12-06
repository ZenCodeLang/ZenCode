package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

import java.util.Arrays;
import java.util.List;

public class IteratorTypeID implements TypeID {
	public final TypeID[] iteratorTypes;
	private final IteratorTypeID normalized;

	public IteratorTypeID(GlobalTypeRegistry registry, TypeID[] iteratorTypes) {
		this.iteratorTypes = iteratorTypes;

		normalized = isDenormalized() ? normalize(registry) : this;
	}

	@Override
	public IteratorTypeID getNormalized() {
		return normalized;
	}

	private boolean isDenormalized() {
		for (TypeID type : iteratorTypes)
			if (type.getNormalized() != type)
				return true;

		return false;
	}

	private IteratorTypeID normalize(GlobalTypeRegistry registry) {
		TypeID[] normalizedTypes = new TypeID[iteratorTypes.length];
		for (int i = 0; i < normalizedTypes.length; i++)
			normalizedTypes[i] = iteratorTypes[i].getNormalized();
		return registry.getIterator(normalizedTypes);
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitIterator(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitIterator(context, this);
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
	public TypeID instance(GenericMapper mapper) {
		TypeID[] instanced = mapper.map(iteratorTypes);
		return mapper.registry.getIterator(instanced);
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		for (TypeID type : iteratorTypes)
			if (type.hasInferenceBlockingTypeParameters(parameters))
				return true;

		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		for (TypeID type : iteratorTypes)
			type.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 13 * hash + Arrays.deepHashCode(this.iteratorTypes);
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
		final IteratorTypeID other = (IteratorTypeID) obj;
		return Arrays.deepEquals(this.iteratorTypes, other.iteratorTypes);
	}
}
