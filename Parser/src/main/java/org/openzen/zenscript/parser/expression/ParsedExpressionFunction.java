package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.LambdaScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

import java.util.Map;

public class ParsedExpressionFunction extends ParsedExpression {
	public final ParsedFunctionHeader header;
	public final ParsedFunctionBody body;

	public ParsedExpressionFunction(CodePosition position, ParsedFunctionHeader header, ParsedFunctionBody body) {
		super(position);

		this.header = header;
		this.body = body;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		FunctionHeader definedHeader = header.compile(scope);
		FunctionHeader header = definedHeader;
		FunctionTypeID type = null;
		for (TypeID hint : scope.hints) {
			if (hint.getNormalized() instanceof FunctionTypeID) {
				FunctionTypeID functionHint = (FunctionTypeID) hint.getNormalized();
				if (header.canOverride(scope, functionHint.header)) {
					if (header != definedHeader)
						return new InvalidExpression(position, hint, CompileExceptionCode.MULTIPLE_MATCHING_HINTS, "Ambiguity trying to resolve function types, can't decide for the type");

					header = functionHint.header.forLambda(definedHeader);
					type = functionHint;
				}
			}
		}

		FunctionHeader genericHeader = header;
		if (!scope.genericInferenceMap.isEmpty()) {
			// prepare for type parameter inference
			header = header.forTypeParameterInference();
		}

		LambdaClosure closure = new LambdaClosure();
		StatementScope innerScope = new LambdaScope(scope, closure, header);
		Statement statements = body.compile(innerScope, header);

		if (header.getReturnType() == BasicTypeID.UNDETERMINED) {
			TypeID returnType = statements.getReturnType();
			if (returnType == null) {
				if(genericHeader.getReturnType() != BasicTypeID.UNDETERMINED) {
					returnType = genericHeader.getReturnType();
				} else {
					returnType = new InvalidTypeID(position, CompileExceptionCode.CANNOT_INFER_RETURN_TYPE, "Could not infer return type");
				}
			}

			header.setReturnType(returnType);
		}


		if (genericHeader.typeParameters.length > 0 && !scope.genericInferenceMap.isEmpty()) {
			// perform type parameter inference
			TypeID returnType = statements.getReturnType();
			if (returnType != null) {
				Map<TypeParameter, TypeID> inferredTypes = returnType.inferTypeParameters(scope.getMemberCache(), genericHeader
						.getReturnType());
				if (inferredTypes == null) {
					throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer generic type parameters");
				}

				scope.genericInferenceMap.putAll(inferredTypes);
			}

		}

		final FunctionHeader thatOtherHeader = genericHeader.withGenericArguments(new GenericMapper(position, scope.getTypeRegistry(), scope.genericInferenceMap));
		if (thatOtherHeader.getReturnType() == BasicTypeID.UNDETERMINED) {
			thatOtherHeader.setReturnType(header.getReturnType());
		}
		if (type == null)
			type = scope.getTypeRegistry().getFunction(thatOtherHeader);
		return new FunctionExpression(position, type, closure, header, statements);
	}

	@Override
	public boolean isCompatibleWith(BaseScope scope, TypeID type) {
		if (type.isOptional())
			type = type.withoutOptional();

		if (type instanceof FunctionTypeID) {
			FunctionHeader definedHeader = header.compile(scope);
			FunctionTypeID targetFunction = (FunctionTypeID) type;
			return definedHeader.canOverride(scope, targetFunction.header);
		} else {
			return false;
		}
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
