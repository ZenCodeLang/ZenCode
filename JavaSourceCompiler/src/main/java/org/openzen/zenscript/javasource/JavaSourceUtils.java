/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceUtils {
	private JavaSourceUtils() {}
	
	public static void formatTypeParameters(JavaSourceFileScope scope, StringBuilder output, TypeParameter[] parameters) {
		if (parameters == null || parameters.length == 0)
			return;
		
		output.append("<");
		for (int i = 0; i < parameters.length; i++) {
			if (i > 0)
				output.append(", ");
			
			TypeParameter typeParameter = parameters[i];
			output.append(typeParameter.name);
			
			if (typeParameter.bounds.size() > 0) {
				output.append(" ");
				// TODO
			}
		}
		output.append("> ");
	}
}
