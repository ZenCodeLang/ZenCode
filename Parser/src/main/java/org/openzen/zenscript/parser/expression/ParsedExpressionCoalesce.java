/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.CoalesceExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionCoalesce extends ParsedExpression {
	private final ParsedExpression left;
	private final ParsedExpression right;
	
	public ParsedExpressionCoalesce(CodePosition position, ParsedExpression left, ParsedExpression right) {
		super(position);
		
		this.left = left;
		this.right = right;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cLeft = left.compile(scope).eval();
		ITypeID cLeftType = cLeft.type;
		if (!cLeftType.isOptional())
			throw new CompileException(position, CompileExceptionCode.COALESCE_TARGET_NOT_OPTIONAL, "Type of the first expression is not optional");
		
		ITypeID resultType = cLeftType.getOptionalBase();
		Expression cRight = right.compile(scope.withHint(resultType)).eval().castImplicit(position, scope, resultType);
		return new CoalesceExpression(position, cLeft, cRight);
	}

	@Override
	public boolean hasStrongType() {
		return left.hasStrongType();
	}
}
