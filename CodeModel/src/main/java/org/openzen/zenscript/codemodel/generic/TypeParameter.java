package org.openzen.zenscript.codemodel.generic;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;

public class TypeParameter extends Taggable {
	public static final TypeParameter[] NONE = new TypeParameter[0];

	public final CodePosition position;
	public final String name;
	public final List<TypeParameterBound> bounds = new ArrayList<>();

	public TypeParameter(CodePosition position, String name) {
		this.position = position;
		this.name = name;
	}

	public void addBound(TypeParameterBound bound) {
		bounds.add(bound);
	}

	public boolean isObjectType() {
		for (TypeParameterBound bound : bounds)
			if (bound.isObjectType())
				return true;

		return false;
	}

	public boolean matches(TypeID type) {
		for (TypeParameterBound bound : bounds) {
			if (!bound.matches(type))
				return false;
		}

		return true;
	}

	public String getCanonical() {
		StringBuilder result = new StringBuilder();
		result.append(name);
		for (TypeParameterBound bound : bounds) {
			result.append(':');
			result.append(bound.getCanonical());
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TypeParameter that = (TypeParameter) o;

		if (!name.equals(that.name))
			return false;
		return bounds.equals(that.bounds);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + bounds.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return name + "@" + position.toShortString();
	}
}
