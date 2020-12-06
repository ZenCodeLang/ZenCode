/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.generic.GenericParameterBoundVisitor;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 * @author Hoofdgebruiker
 */
public class JavaSourceUtils {
	private JavaSourceUtils() {
	}

	public static void formatTypeParameters(JavaSourceTypeVisitor typeFormatter, StringBuilder output, TypeParameter[] parameters, boolean space) {
		if (parameters == null || parameters.length == 0)
			return;

		TypeParameterBoundVisitor boundVisitor = new TypeParameterBoundVisitor(typeFormatter, output);
		output.append("<");
		for (int i = 0; i < parameters.length; i++) {
			if (i > 0)
				output.append(", ");

			TypeParameter typeParameter = parameters[i];
			output.append(typeParameter.name);

			if (typeParameter.bounds.size() > 0) {
				for (TypeParameterBound bound : typeParameter.bounds)
					bound.accept(boundVisitor);
			}
		}
		output.append(">");
		if (space)
			output.append(" ");
	}

	public static void formatTypeParameters(JavaSourceTypeVisitor typeFormatter, StringBuilder output, TypeParameter[] expansion, TypeParameter[] parameters) {
		if (((parameters == null ? 0 : parameters.length) + (expansion == null ? 0 : expansion.length)) == 0)
			return;

		TypeParameterBoundVisitor boundVisitor = new TypeParameterBoundVisitor(typeFormatter, output);
		output.append("<");
		boolean first = true;
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				if (first)
					first = false;
				else
					output.append(", ");

				TypeParameter typeParameter = parameters[i];
				output.append(typeParameter.name);

				if (typeParameter.bounds.size() > 0) {
					for (TypeParameterBound bound : typeParameter.bounds)
						bound.accept(boundVisitor);
				}
			}
		}
		if (expansion != null) {
			for (int i = 0; i < expansion.length; i++) {
				if (first)
					first = false;
				else
					output.append(", ");

				TypeParameter typeParameter = expansion[i];
				output.append(typeParameter.name);

				if (typeParameter.bounds.size() > 0) {
					for (TypeParameterBound bound : typeParameter.bounds)
						bound.accept(boundVisitor);
				}
			}
		}
		output.append("> ");
	}

	private static class TypeParameterBoundVisitor implements GenericParameterBoundVisitor<Void> {
		private final JavaSourceTypeVisitor typeFormatter;
		private final StringBuilder output;

		public TypeParameterBoundVisitor(JavaSourceTypeVisitor typeFormatter, StringBuilder output) {
			this.typeFormatter = typeFormatter;
			this.output = output;
		}

		@Override
		public Void visitSuper(ParameterSuperBound bound) {
			output.append(" super ").append(bound.type.accept(typeFormatter));
			return null;
		}

		@Override
		public Void visitType(ParameterTypeBound bound) {
			output.append(" extends ").append(bound.type.accept(typeFormatter));
			return null;
		}
	}
}
