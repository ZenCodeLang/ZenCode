/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.Map;
import java.util.Objects;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionParameter {
	public int index = -1;
	public final ITypeID type;
	public final String name;
	public final Expression defaultValue;
	public final boolean variadic;
	
	public FunctionParameter(ITypeID type) {
		this.type = type;
		this.name = "";
		this.defaultValue = null;
		this.variadic = false;
	}
	
	public FunctionParameter(ITypeID type, String name) {
		this.type = type;
		this.name = name;
		this.defaultValue = null;
		this.variadic = false;
	}
	
	public FunctionParameter(ITypeID type, String name, Expression defaultValue, boolean variadic) {
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
		this.variadic = variadic;
	}
	
	public FunctionParameter withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		return new FunctionParameter(type.withGenericArguments(registry, arguments), name, defaultValue, variadic);
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
		if (this.variadic != other.variadic) {
			return false;
		}
		if (!Objects.equals(this.type, other.type)) {
			return false;
		}
		return true;
	}
}
