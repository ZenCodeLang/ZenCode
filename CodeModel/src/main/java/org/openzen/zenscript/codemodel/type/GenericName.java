/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericName {
	public final String name;
	public final List<ITypeID> arguments;
	
	public GenericName(String name) {
		this(name, Collections.emptyList());
	}
	
	public GenericName(String name, List<ITypeID> arguments) {
		this.name = name;
		this.arguments = arguments;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(name);
		if (!arguments.isEmpty()) {
			result.append("<");
			result.append(">");
		}
		return result.toString();
	}
}
