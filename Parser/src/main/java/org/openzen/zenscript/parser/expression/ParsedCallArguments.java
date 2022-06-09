package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.InferenceBlockingTypeParameterVisitor;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;

import java.util.*;
import java.util.stream.Collectors;

public class ParsedCallArguments {
	public static final ParsedCallArguments NONE = new ParsedCallArguments(Collections.emptyList());
	public final List<ParsedExpression> arguments;

	public ParsedCallArguments(List<ParsedExpression> arguments) {
		this.arguments = arguments;
	}

	public static ParsedCallArguments parse(ZSTokenParser tokens) throws ParseException {
		tokens.required(ZSTokenType.T_BROPEN, "( expected");

		List<ParsedExpression> arguments = new ArrayList<>();
		try {
			if (tokens.optional(ZSTokenType.T_BRCLOSE) == null) {
				do {
					arguments.add(ParsedExpression.parse(tokens));
				} while (tokens.optional(ZSTokenType.T_COMMA) != null);
				tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
			tokens.recoverUntilOnToken(ZSTokenType.T_BRCLOSE);
		}

		return new ParsedCallArguments(arguments);
	}

	public static ParsedCallArguments parseForAnnotation(ZSTokenParser tokens) throws ParseException {
		List<ParsedExpression> arguments = new ArrayList<>();
		if (tokens.isNext(ZSTokenType.T_BROPEN)) {
			tokens.required(ZSTokenType.T_BROPEN, "( expected");
			try {
				if (tokens.optional(ZSTokenType.T_BRCLOSE) == null) {
					do {
						arguments.add(ParsedExpression.parse(tokens));
					} while (tokens.optional(ZSTokenType.T_COMMA) != null);
					tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
				}
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilOnToken(ZSTokenType.T_BRCLOSE);
			}
		}

		return new ParsedCallArguments(arguments);
	}

	public CompilingExpression[] compile(ExpressionCompiler compiler) {
		return arguments.stream().map(arg -> arg.compile(compiler)).toArray(CompilingExpression[]::new);
	}

	public CallArguments compileCall(
			CodePosition position,
			ExpressionScope scope,
			TypeID[] genericParameters,
			TypeMemberGroup member) throws CompileException {
		List<FunctionHeader> possibleHeaders = member.getMethodMembers().stream()
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
		return compileCall(position, scope, genericParameters, possibleHeaders);
	}

	public CallArguments compileCall(
			CodePosition position,
			ExpressionScope scope,
			TypeID[] typeArguments,
			List<FunctionHeader> candidateFunctions) throws CompileException {
		List<FunctionHeader> candidates = new ArrayList<>();
		for (FunctionHeader header : candidateFunctions) {
			if (isCompatibleWith(scope, header, typeArguments))
				candidates.add(header);
		}

		if (candidates.isEmpty()) {
			StringBuilder explanation = new StringBuilder("No compatible methods found");
			CallArguments arguments = compileCallNaive(position, scope);
			if(!candidateFunctions.isEmpty()) {
				explanation.append(":\n");
				for (FunctionHeader candidate : candidateFunctions)
					explanation.append(candidate.explainWhyIncompatible(scope, arguments)).append("\n");
			}else{
				explanation.append("!");
			}
			throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, explanation.toString());
		}

		ExpressionScope innerScope = scope;
		if (candidates.size() == 1) {
			innerScope = scope.forCall(candidates.get(0));
		} else {
			int givenTypeArguments = typeArguments == null ? 0 : typeArguments.length;
			candidates = candidates.stream()
					.filter(candidate -> candidate.getNumberOfTypeParameters() == givenTypeArguments)
					.collect(Collectors.toList());

			if (candidates.isEmpty()) {
				throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "Could not determine call type parameters");
			}
		}

		List<TypeID>[] predictedTypes = new List[arguments.size()];
		for (int i = 0; i < predictedTypes.length; i++)
			predictedTypes[i] = new ArrayList<>();

		for (FunctionHeader header : candidates) {

			type_parameter_replacement:
			if (typeArguments != null && typeArguments.length > 0 && header.typeParameters.length == typeArguments.length) {
				final Map<TypeParameter, TypeID> types = new HashMap<>();
				for (int i = 0; i < header.typeParameters.length; i++) {
					if (!header.typeParameters[i].matches(scope.getMemberCache(), typeArguments[i])) {
						break type_parameter_replacement;
					}
					types.put(header.typeParameters[i], typeArguments[i]);
				}
				header = header.withGenericArguments(new GenericMapper(position, scope.getTypeRegistry(), types));
			}

			// Use Parameters.length instead of maxParameters to get number of declared parameters
			// We could be variadic if we have 0 vararg parameters (=#parameters-1 arguments), 1 (=#parameters arguments) or more
			// We could be "normal" if we have as many args as parameters, or less (for optionals)
			boolean couldBeVariadic = header.isVariadic() && arguments.size() >= header.parameters.length - 1;
			boolean couldBeNormalCall = arguments.size() <= header.parameters.length;

			for (int i = 0; i < arguments.size(); i++) {

				if(couldBeVariadic) {
					final TypeID parameterType = header.getParameterType(true, i);
					if (!predictedTypes[i].contains(parameterType))
						predictedTypes[i].add(parameterType);
				}
				if(couldBeNormalCall) {
					final TypeID parameterType = header.getParameterType(false, i);
					if (!predictedTypes[i].contains(parameterType))
						predictedTypes[i].add(parameterType);
				}
			}
		}

		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(innerScope.withHints(predictedTypes[i]));
			cArguments[i] = cArgument.eval();
		}

		TypeID[] typeArguments2 = typeArguments;
		if (typeArguments2 == null || typeArguments2.length == 0) {
			for (FunctionHeader candidate : candidates) {
				typeArguments2 = new TypeID[candidate.typeParameters.length];
				for (int i = 0; i < typeArguments2.length; i++) {
					if (innerScope.genericInferenceMap.get(candidate.typeParameters[i]) == null)
						typeArguments2[i] = new InvalidTypeID(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer type parameter " + candidate.typeParameters[i].name);
					else
						typeArguments2[i] = innerScope.genericInferenceMap.get(candidate.typeParameters[i]);
				}

				break;
			}
		}

		return new CallArguments(typeArguments2, cArguments);
	}


	public CallArguments compileCall(
			CodePosition position,
			ExpressionScope scope,
			TypeID[] typeArguments,
			FunctionHeader function) throws CompileException {
		ExpressionScope innerScope = scope.forCall(function);

		List<TypeID>[] predictedTypes = new List[arguments.size()];
		for (int i = 0; i < predictedTypes.length; i++) {
			predictedTypes[i] = new ArrayList<>();
			predictedTypes[i].add(function.parameters[i].type);
		}

		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(innerScope.withHints(predictedTypes[i]));
			cArguments[i] = cArgument.eval();
		}

		TypeID[] typeArguments2 = typeArguments;
		if (typeArguments2 == null) {
			typeArguments2 = new TypeID[function.typeParameters.length];
			for (int i = 0; i < typeArguments2.length; i++) {
				if (innerScope.genericInferenceMap.get(function.typeParameters[i]) == null)
					throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer type parameter " + function.typeParameters[i].name);
				else
					typeArguments2[i] = innerScope.genericInferenceMap.get(function.typeParameters[i]);
			}
		}

		return new CallArguments(typeArguments2, cArguments);
	}

	private CallArguments compileCallNaive(CodePosition position, ExpressionScope scope) throws CompileException {
		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(scope);
			cArguments[i] = cArgument.eval();
		}
		return new CallArguments(TypeID.NONE, cArguments);
	}

	private boolean isCompatibleWith(BaseScope scope, FunctionHeader header, TypeID[] typeArguments) {
		if (!header.accepts(arguments.size()))
			return false;

		//TODO: This is wrong
		boolean variadic = header.isVariadic();
		InferenceBlockingTypeParameterVisitor inferenceBlockingVisitor = new InferenceBlockingTypeParameterVisitor(header.typeParameters);
		for (int i = 0; i < arguments.size(); i++) {
			FunctionParameter parameter = header.getParameter(variadic, i);
			if (typeArguments == null && parameter.type.accept(inferenceBlockingVisitor))
				return false;

			if (!arguments.get(i).isCompatibleWith(scope, header.getParameterType(variadic, i).getNormalized()))
				return false;
		}

		return true;
	}
}
