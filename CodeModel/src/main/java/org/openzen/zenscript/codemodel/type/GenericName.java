/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericName {
	public final String name;
	public final ITypeID[] arguments;
	
	public GenericName(String name) {
		this(name, null);
	}
	
	public GenericName(String name, ITypeID[] arguments) {
		this.name = name;
		this.arguments = arguments;
	}
	
	public int getNumberOfArguments() {
		return arguments == null ? 0 : arguments.length;
	}
	
	public boolean hasArguments() {
		return arguments != null && arguments.length > 0;
	}
	
	public boolean hasNoArguments() {
		return arguments == null || arguments.length == 0;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(name);
		if (arguments != null) {
			result.append("<");
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0)
					result.append(", ");
				result.append(arguments[i].toString());
			}
			result.append(">");
		}
		return result.toString();
	}
}
