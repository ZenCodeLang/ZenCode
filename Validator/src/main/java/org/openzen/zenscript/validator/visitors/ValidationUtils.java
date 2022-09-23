/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.*;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.validator.TypeContext;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.openzen.zenscript.validator.ValidationLogEntry.Code.*;

/**
 * @author Hoofdgebruiker
 */
public class ValidationUtils {
	private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");

	private ValidationUtils() {
	}

	public static void validateValidOverride(Validator target, CodePosition position, FunctionHeader header, FunctionHeader overridden) {
		if (!header.canOverride(target.resolver, overridden))
			target.logError(position, CompileErrors.invalidOverride("Invalid override: incompatible parameters or return type"));
	}

	public static void validateIdentifier(Validator target, CodePosition position, String identifier) {
		if (identifier == null || !IDENTIFIER_PATTERN.matcher(identifier).matches()) {
			target.logError(position, CompileErrors.invalidIdentifier(identifier));
		}
	}

	public static void validateHeader(Validator target, CodePosition position, FunctionHeader header) {
		TypeValidator typeValidator = new TypeValidator(target, position);
		typeValidator.validate(TypeContext.RETURN_TYPE, header.getReturnType());

		Set<String> parameterNames = new HashSet<>();
		int i = 0;
		for (FunctionParameter parameter : header.parameters) {
			if (parameterNames.contains(parameter.name)) {
				target.logError(position, CompileErrors.duplicateParameterName(parameter.name));
			}

			parameterNames.add(parameter.name);
			typeValidator.validate(TypeContext.PARAMETER_TYPE, parameter.type);

			final Expression defaultValue = parameter.defaultValue;
			if (defaultValue != null) {
				defaultValue.accept(new ExpressionValidator(target, new DefaultParameterValueExpressionScope()));
				typeValidator.validate(TypeContext.PARAMETER_TYPE, defaultValue.type);
				if (!defaultValue.type.equals(parameter.type) && !target.resolver.resolve(defaultValue.type).canCastImplicitlyTo(parameter.type)) {
					target.logError(position, CompileErrors.typeMismatch(parameter.type, defaultValue.type));
				}
			}

			if (parameter.variadic) {
				if (i != header.parameters.length - 1) {
					target.logError(position, CompileErrors.variadicParameterMustBeLast());
				}
				if (!(parameter.type instanceof ArrayTypeID)) {
					target.logError(position, CompileErrors.variadicParameterMustBeArray());
				}
			}
			i++;
		}
	}

	public static void validateModifiers(
			Validator target,
			Modifiers modifiers,
			int allowedModifiers,
			CodePosition position,
			String error) {
		if (modifiers.isPublic() && modifiers.isInternal())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine public and internal"));
		if (modifiers.isPublic() && modifiers.isPrivate())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine public and private"));
		if (modifiers.isPublic() && modifiers.isProtected())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine public and protected"));
		if (modifiers.isInternal() && modifiers.isPrivate())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine internal and private"));
		if (modifiers.isInternal() && modifiers.isProtected())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine internal and protected"));
		if (modifiers.isPrivate() && modifiers.isProtected())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine private and protected"));

		if (modifiers.isConst() && modifiers.isConstOptional())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine const and const?"));
		if (modifiers.isFinal() && modifiers.isAbstract())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine abstract and final"));
		if (modifiers.isFinal() && modifiers.isVirtual())
			target.logError(position, CompileErrors.invalidModifier(error + ": cannot combine final and virtual"));

		Modifiers invalid = new Modifiers(modifiers.value & ~allowedModifiers);
		if (invalid.value == 0)
			return;

		if (invalid.isPublic())
			target.logError(position, CompileErrors.invalidModifier(error + ": public not allowed here"));
		if (invalid.isInternal())
			target.logError(position, CompileErrors.invalidModifier(error + ": internal not allowed here"));
		if (invalid.isProtected())
			target.logError(position, CompileErrors.invalidModifier(error + ": protected not allowed here"));
		if (invalid.isPrivate())
			target.logError(position, CompileErrors.invalidModifier(error + ": private not allewed here"));
		if (modifiers.isFinal())
			target.logError(position, CompileErrors.invalidModifier(error + ": final not allowed here"));
		if (modifiers.isConst())
			target.logError(position, CompileErrors.invalidModifier(error + ": const not allowed here"));
		if (modifiers.isConstOptional())
			target.logError(position, CompileErrors.invalidModifier(error + ": const? not allowed here"));
		if (modifiers.isStatic())
			target.logError(position, CompileErrors.invalidModifier(error + ": static not allowed here"));
		if (modifiers.isImplicit())
			target.logError(position, CompileErrors.invalidModifier(error + ": implicit not allowed here"));
		if (modifiers.isVirtual())
			target.logError(position, CompileErrors.invalidModifier(error + ": virtual not allowed here"));
	}

	public static void validateTypeArguments(
			Validator target,
			CodePosition position,
			TypeParameter[] typeParameters,
			TypeID[] typeArguments) {

		int expectedTypeArguments = typeParameters == null ? 0 : typeParameters.length;
		int actualTypeArguments = typeArguments == null ? 0 : typeArguments.length;
		if (expectedTypeArguments != actualTypeArguments) {
			target.logError(position, CompileErrors.invalidNumberOfTypeArguments(expectedTypeArguments, actualTypeArguments));
		}

		for (int i = 0; i < typeParameters.length; i++) {
			TypeParameter typeParameter = typeParameters[i];
			for (TypeParameterBound bound : typeParameter.bounds) {
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
		private final TypeID type;
		private final Validator target;

		public TypeParameterBoundErrorVisitor(TypeID type, Validator target) {
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
		public boolean isFieldInitialized(FieldSymbol field) {
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
