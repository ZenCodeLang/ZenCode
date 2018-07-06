/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.codemodel.generic.GenericParameterBoundVisitor;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
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
		
		TypeParameterBoundVisitor boundVisitor = new TypeParameterBoundVisitor(scope, output);
		output.append("<");
		for (int i = 0; i < parameters.length; i++) {
			if (i > 0)
				output.append(", ");
			
			TypeParameter typeParameter = parameters[i];
			output.append(typeParameter.name);
			
			if (typeParameter.bounds.size() > 0) {
				for (GenericParameterBound bound : typeParameter.bounds)
					bound.accept(boundVisitor);
			}
		}
		output.append("> ");
	}
	
	private static class TypeParameterBoundVisitor implements GenericParameterBoundVisitor<Void> {
		private final JavaSourceFileScope scope;
		private final StringBuilder output;
		
		public TypeParameterBoundVisitor(JavaSourceFileScope scope, StringBuilder output) {
			this.scope = scope;
			this.output = output;
		}

		@Override
		public Void visitSuper(ParameterSuperBound bound) {
			output.append(" super ").append(scope.type(bound.type));
			return null;
		}

		@Override
		public Void visitType(ParameterTypeBound bound) {
			output.append(" extends ").append(scope.type(bound.type));
			return null;
		}
	}
}
