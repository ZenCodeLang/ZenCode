/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.LambdaScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Stan
 */
public class ParsedExpressionFunction extends ParsedExpression {
	public final ParsedFunctionHeader header;
	public final ParsedFunctionBody body;
	
	public ParsedExpressionFunction(CodePosition position, ParsedFunctionHeader header, ParsedFunctionBody body) {
		super(position);
		
		this.header = header;
		this.body = body;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		FunctionHeader definedHeader = header.compile(scope);
		FunctionHeader header = definedHeader;
		for (ITypeID hint : scope.hints) {
			if (hint instanceof FunctionTypeID) {
				FunctionTypeID functionHint = (FunctionTypeID) hint;
				if (header.canCastTo(scope, functionHint.header)) {
					if (header != definedHeader)
						throw new CompileException(position, CompileExceptionCode.MULTIPLE_MATCHING_HINTS, "Ambiguity trying to resolve function types, can't decide for the type");
					
					header = functionHint.header.forLambda(definedHeader);
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
		if (!scope.genericInferenceMap.isEmpty()) {
			// perform type parameter inference
			ITypeID returnType = statements.getReturnType();
			Map<TypeParameter, ITypeID> inferredTypes = new HashMap<>();
			if (!genericHeader.returnType.inferTypeParameters(scope.getMemberCache(), returnType, inferredTypes))
				throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer generic type parameters");
			
			for (Map.Entry<TypeParameter, ITypeID> type : inferredTypes.entrySet()) {
				scope.genericInferenceMap.put(type.getKey(), type.getValue());
			}
		}
		
		FunctionTypeID functionType = scope.getTypeRegistry().getFunction(genericHeader.withGenericArguments(scope.getTypeRegistry(), scope.genericInferenceMap));
		return new FunctionExpression(position, functionType, closure, statements);
	}
	
	@Override
	public boolean isCompatibleWith(BaseScope scope, ITypeID type) {
		if (type instanceof FunctionTypeID) {
			FunctionHeader definedHeader = header.compile(scope);
			FunctionTypeID targetFunction = (FunctionTypeID) type;
			return definedHeader.canCastTo(scope, targetFunction.header);
		} else {
			return false;
		}
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
