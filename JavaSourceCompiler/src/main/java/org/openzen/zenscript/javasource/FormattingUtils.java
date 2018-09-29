/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.formattershared.ExpressionString;
import org.openzen.zenscript.formattershared.StatementFormattingTarget;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class FormattingUtils {
	private FormattingUtils() {}
	
	public static void formatTypeParameters(StringBuilder result, TypeParameter[] parameters, JavaSourceTypeVisitor typeFormatter) {
		if (parameters != null) {
			result.append("<");
			int index = 0;
			for (TypeParameter parameter : parameters) {
				if (index > 0)
					result.append(", ");
				
				result.append(parameter.name);
				
				if (parameter.bounds.size() > 0) {
					for (TypeParameterBound bound : parameter.bounds) {
						result.append(": ");
						result.append(bound.accept(typeFormatter));
					}
				}
				
				index++;
			}
			result.append(">");
		}
	}
	
	public static void formatCall(StringBuilder result, StatementFormattingTarget target, JavaSourceStatementScope scope, CallArguments arguments) {
		if (arguments == null || arguments.typeArguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");
		
		if (arguments.typeArguments.length > 0) {
			result.append("<");
			
			int index = 0;
			for (TypeID typeArgument : arguments.typeArguments) {
				if (index > 0)
					result.append(", ");
				result.append(scope.type(typeArgument));
				index++;
			}
			result.append(">");
		}
		result.append("(");
		int index = 0;
		for (Expression argument : arguments.arguments) {
			if (index > 0)
				result.append(", ");
			result.append(scope.expression(target, argument).value);
			index++;
		}
		result.append(")");
	}
	
	public static void formatExpansionCall(StringBuilder result, StatementFormattingTarget formattingTarget, JavaSourceStatementScope scope, ExpressionString target, CallArguments arguments) {
		if (arguments == null || arguments.typeArguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");
		
		if (arguments.typeArguments.length > 0) {
			result.append("<");
			
			int index = 0;
			for (TypeID typeArgument : arguments.typeArguments) {
				if (index > 0)
					result.append(", ");
				result.append(scope.type(typeArgument));
				index++;
			}
			result.append(">");
		}
		result.append("(");
		result.append(target.value);
		for (Expression argument : arguments.arguments) {
			result.append(", ");
			result.append(scope.expression(formattingTarget, argument).value);
		}
		result.append(")");
	}
}
