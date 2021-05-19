package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.annotations.ParameterAnnotation;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Objects;

public class FunctionParameter extends Taggable {
	public static final FunctionParameter[] NONE = new FunctionParameter[0];
	public final TypeID type;
	public final String name;
	public final boolean variadic;
	public ParameterAnnotation[] annotations;
	public Expression defaultValue;

	public FunctionParameter(TypeID type) {
		this.annotations = ParameterAnnotation.NONE;
		this.type = type;
		this.name = "";
		this.defaultValue = null;
		this.variadic = false;
	}

	public FunctionParameter(TypeID type, String name) {
		this.annotations = ParameterAnnotation.NONE;
		this.type = type;
		this.name = name;
		this.defaultValue = null;
		this.variadic = false;
	}

	public FunctionParameter(TypeID type, String name, Expression defaultValue, boolean variadic) {
		this.annotations = ParameterAnnotation.NONE;
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
		this.variadic = variadic;
	}

	public FunctionParameter(TypeID type, String name, boolean variadic) {
		this(type, name, null, variadic);
	}

	public FunctionParameter normalize(GlobalTypeRegistry registry) {
		FunctionParameter result = new FunctionParameter(type.getNormalized(), name, defaultValue, variadic);
		result.annotations = this.annotations;
		return result;
	}

	public FunctionParameter withGenericArguments(GenericMapper mapper) {
		TypeID instanced = type.instance(mapper);
		if (instanced.equals(type))
			return this;

		FunctionParameter result = new FunctionParameter(instanced, name, defaultValue, variadic);
		result.annotations = annotations;
		result.addAllTagsFrom(this); // TODO: this will cause trouble -> references?
		return result;
	}

	@Override
	public String toString() {
		return name + " as " + type.toString();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 17 * hash + Objects.hashCode(this.type);
		hash = 17 * hash + (this.variadic ? 1 : 0);
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
		final FunctionParameter other = (FunctionParameter) obj;
		return this.variadic == other.variadic && this.type.equals(other.type);
	}
}
