package org.openzen.zenscript.codemodel.type;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.builtin.RangeTypeSymbol;

import java.util.List;
import java.util.Optional;

public class RangeTypeID implements TypeID {
	public static final RangeTypeID INT = new RangeTypeID(BasicTypeID.INT);
	public static final RangeTypeID USIZE = new RangeTypeID(BasicTypeID.USIZE);

	public final TypeID baseType;

	public RangeTypeID(TypeID baseType) {
		this.baseType = baseType;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return new RangeTypeID(baseType.instance(mapper));
	}

	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitRange(this);
	}

	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitRange(context, this);
	}

	@Override
	public Optional<RangeTypeID> asRange() { return Optional.of(this); }

	@Override
	public ResolvingType resolve() {
		return RangeTypeSymbol.INSTANCE.resolve(new TypeID[] { baseType });
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isValueType() {
		return baseType.isValueType();
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		baseType.extractTypeParameters(typeParameters);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + baseType.hashCode();
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
		return this.baseType.equals(other.baseType);
	}

	@Override
	public String toString() {
		return baseType + " .. " + baseType;
	}
}
