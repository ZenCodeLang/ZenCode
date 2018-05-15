/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.TryRethrowAsExceptionExpression;
import org.openzen.zenscript.codemodel.expression.TryRethrowAsResultExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTryRethrowExpression extends ParsedExpression {
	private final ParsedExpression source;
	
	public ParsedTryRethrowExpression(CodePosition position, ParsedExpression source) {
		super(position);
		
		this.source = source;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		HighLevelDefinition result = scope.getTypeRegistry().stdlib.getDefinition("Result");
		
		Expression cSource = source.compile(scope).eval();
		if (cSource.thrownType != null) {
			// expression throws
			if (scope.getFunctionHeader() == null || scope.getFunctionHeader().thrownType != null) {
				// rethrow as exception
				return new TryRethrowAsExceptionExpression(position, cSource.type, cSource, cSource.thrownType);
			} else {
				// rethrow as result
				ITypeID resultType = scope.getTypeRegistry().getForDefinition(result, cSource.type, cSource.thrownType);
				return new TryRethrowAsResultExpression(position, resultType, cSource);
			}
		} else {
			// expression
			if (cSource.type instanceof DefinitionTypeID) {
				DefinitionTypeID sourceType = (DefinitionTypeID)cSource.type;
				if (sourceType.definition == result)
					return new TryRethrowAsExceptionExpression(position, sourceType.typeParameters[0], cSource, sourceType.typeParameters[1]);
			}
			
			if (scope.getFunctionHeader() == null)
				throw new CompileException(position, CompileExceptionCode.TRY_RETHROW_NOT_A_RESULT, "type is not a Result type, cannot convert");
			
			throw new CompileException(position, CompileExceptionCode.TRY_RETHROW_NOT_A_RESULT, "this expression doesn't throw an exception nor returns a result");
		}
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
