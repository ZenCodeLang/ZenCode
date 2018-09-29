/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.Objects;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.annotations.ParameterAnnotation;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionParameter extends Taggable {
	public static final FunctionParameter[] NONE = new FunctionParameter[0];
	
	public ParameterAnnotation[] annotations;
	public final StoredType type;
	public final String name;
	public Expression defaultValue;
	public final boolean variadic;
	
	public FunctionParameter(StoredType type) {
		this.annotations = ParameterAnnotation.NONE;
		this.type = type;
		this.name = "";
		this.defaultValue = null;
		this.variadic = false;
	}
	
	public FunctionParameter(BasicTypeID type) {
		this(type.stored);
	}
	
	public FunctionParameter(StoredType type, String name) {
		this.annotations = ParameterAnnotation.NONE;
		this.type = type;
		this.name = name;
		this.defaultValue = null;
		this.variadic = false;
	}
	
	public FunctionParameter(StoredType type, String name, Expression defaultValue, boolean variadic) {
		this.annotations = ParameterAnnotation.NONE;
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
		this.variadic = variadic;
	}
	
	public FunctionParameter normalized(GlobalTypeRegistry registry) {
		FunctionParameter result = new FunctionParameter(type.getNormalized(), name, defaultValue, variadic);
		result.annotations = this.annotations;
		return result;
	}
	
	public FunctionParameter withGenericArguments(GenericMapper mapper) {
		StoredType instanced = type.instance(mapper);
		if (instanced == type)
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
