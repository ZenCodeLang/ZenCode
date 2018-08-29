/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.codemodel.generic.GenericParameterBoundVisitor;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.validator.ValidationLogEntry;
import static org.openzen.zenscript.validator.ValidationLogEntry.Code.*;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ValidationUtils {
	private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");

	public static void validateValidOverride(Validator target, CodePosition position, TypeScope scope, FunctionHeader header, FunctionHeader overridden) {
		if (!header.canOverride(scope, overridden))
			target.logError(INVALID_OVERRIDE, position, "Invalid override: incompatible parameters or return type");
	}
	
	private ValidationUtils() {}
	
	public static void validateIdentifier(Validator target, CodePosition position, String identifier) { 
		if (identifier == null || !IDENTIFIER_PATTERN.matcher(identifier).matches()) {
			target.logError(INVALID_IDENTIFIER, position, "Invalid identifier: " + identifier);
		}
	}
	
	public static void validateHeader(Validator target, CodePosition position, FunctionHeader header) {
		TypeValidator typeValidator = new TypeValidator(target, position);
		header.getReturnType().accept(typeValidator);
		
		Set<String> parameterNames = new HashSet<>();
		int i = 0;
		for (FunctionParameter parameter : header.parameters) {
			if (parameterNames.contains(parameter.name)) {
				target.logError(DUPLICATE_PARAMETER_NAME, position, "Duplicate parameter name: " + parameter.name);
			}
			
			parameterNames.add(parameter.name);
			parameter.type.accept(typeValidator);
			
			if (parameter.defaultValue != null) {
				parameter.defaultValue.accept(new ExpressionValidator(target, new DefaultParameterValueExpressionScope()));
				if (parameter.defaultValue.type != parameter.type) {
					target.logError(INVALID_TYPE, position, "default value does not match parameter type");
				}
			}
			
			if (parameter.variadic) {
				if (i != header.parameters.length - 1) {
					target.logError(VARIADIC_PARAMETER_MUST_BE_LAST, position, "variadic parameter must be the last parameter");
				}
				if (!(parameter.type instanceof ArrayTypeID)) {
					target.logError(INVALID_TYPE, position, "variadic parameter must be an array");
				}
			}
			i++;
		}
	}
	
	public static void validateModifiers(
			Validator target,
			int modifiers,
			int allowedModifiers,
			CodePosition position,
			String error)
	{
		if (Modifiers.isPublic(modifiers) && Modifiers.isExport(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine public and export");
		if (Modifiers.isPublic(modifiers) && Modifiers.isPrivate(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine public and private");
		if (Modifiers.isPublic(modifiers) && Modifiers.isProtected(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine public and protected");
		if (Modifiers.isExport(modifiers) && Modifiers.isPrivate(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine export and private");
		if (Modifiers.isExport(modifiers) && Modifiers.isProtected(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine export and protected");
		if (Modifiers.isPrivate(modifiers) && Modifiers.isProtected(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine private and protected");
		
		if (Modifiers.isConst(modifiers) && Modifiers.isConstOptional(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine const and const?");
		if (Modifiers.isFinal(modifiers) && Modifiers.isAbstract(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine abstract and final");
		if (Modifiers.isFinal(modifiers) && Modifiers.isVirtual(modifiers))
			target.logError(INVALID_MODIFIER, position, error + ": cannot combine final and virtual");
		
		int invalid = modifiers & ~allowedModifiers;
		if (invalid == 0)
			return;
		
		if (Modifiers.isPublic(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": public");
		if (Modifiers.isExport(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": export");
		if (Modifiers.isProtected(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": protected");
		if (Modifiers.isPrivate(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": private");
		if (Modifiers.isFinal(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": final");
		if (Modifiers.isConst(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": const");
		if (Modifiers.isConstOptional(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": const?");
		if (Modifiers.isStatic(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": static");
		if (Modifiers.isImplicit(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": implicit");
		if (Modifiers.isVirtual(invalid))
			target.logError(INVALID_MODIFIER, position, error + ": virtual");
	}
	
	public static void validateTypeArguments(
			Validator target,
			CodePosition position,
			TypeParameter[] typeParameters,
			ITypeID[] typeArguments)
	{
		if (typeParameters == null || typeParameters.length == 0) {
			if (typeArguments == null || typeArguments.length == 0) {
				return;
			} else {
				target.logError(
					ValidationLogEntry.Code.INVALID_TYPE_ARGUMENT,
					position,
					"Invalid number of type arguments: "
							+ typeArguments.length
							+ " arguments given but none expected");
				return;
			}
		}
		if (typeArguments == null || typeArguments.length == 0) {
			target.logError(
					ValidationLogEntry.Code.INVALID_TYPE_ARGUMENT,
					position,
					"Invalid number of type arguments: "
							+ typeParameters.length
							+ " arguments expected but none given");
			return;
		}
		
		if (typeParameters.length != typeArguments.length) {
			target.logError(
					ValidationLogEntry.Code.INVALID_TYPE_ARGUMENT,
					position,
					"Invalid number of type arguments: "
							+ typeArguments.length
							+ " arguments given but "
							+ typeParameters.length
							+ " expected");
			return;
		}
		
		for (int i = 0; i < typeParameters.length; i++) {
			TypeParameter typeParameter = typeParameters[i];
			for (GenericParameterBound bound : typeParameter.bounds) {
				// TODO - obtain member cache for validation
				/*if (!bound.matches(typeArguments[i])) {
					target.logError(
							ValidationLogEntry.Code.INVALID_TYPE_ARGUMENT,
							position,
							bound.accept(new TypeParameterBoundErrorVisitor(typeArguments[i], target)));
					isValid = false;
				}*/
			}
		}
	}
	
	private static class TypeParameterBoundErrorVisitor implements GenericParameterBoundVisitor<String> {
		private final ITypeID type;
		private final Validator target;
		
		public TypeParameterBoundErrorVisitor(ITypeID type, Validator target) {
			this.type = type;
			this.target = target;
		}

		@Override
		public String visitSuper(ParameterSuperBound bound) {
			return type.toString() + " is not a superclass of " + bound.type.toString();
		}

		@Override
		public String visitType(ParameterTypeBound bound) {
			return type.toString() + " is does not extend or implement " + bound.type.toString();
		}
	}
	
	private static class DefaultParameterValueExpressionScope implements ExpressionScope {
		
		@Override
		public boolean isConstructor() {
			return false;
		}

		@Override
		public boolean isFirstStatement() {
			return true;
		}

		@Override
		public boolean hasThis() {
			return false;
		}

		@Override
		public boolean isFieldInitialized(FieldMember field) {
			return false;
		}

		@Override
		public void markConstructorForwarded() {
			
		}

		@Override
		public boolean isEnumConstantInitialized(EnumConstantMember member) {
			return true;
		}

		@Override
		public boolean isLocalVariableInitialized(VarStatement variable) {
			return false;
		}

		@Override
		public boolean isStaticInitializer() {
			return false;
		}

		@Override
		public HighLevelDefinition getDefinition() {
			return null;
		}
	}
}
