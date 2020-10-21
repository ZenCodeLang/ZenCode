/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.CoalesceExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

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
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		Expression cLeft = left.compile(scope).eval();
		TypeID cLeftType = cLeft.type;
		if (!cLeftType.isOptional())
			return new InvalidExpression(position, cLeft.type, CompileExceptionCode.COALESCE_TARGET_NOT_OPTIONAL, "Type of the first expression is not optional");

		TypeID resultType = cLeftType.withoutOptional();
		Expression cRight = right.compile(scope.withHint(resultType)).eval();
		
		TypeMembers resultTypeMembers = scope.getTypeMembers(resultType);
		resultType = resultTypeMembers.union(cRight.type);
		cLeft = cLeft.castImplicit(position, scope, resultType.isOptional() ? resultType : scope.getTypeRegistry().getOptional(resultType));
		cRight = cRight.castImplicit(position, scope, resultType);
		
		return new CoalesceExpression(position, cLeft, cRight);
	}

	@Override
	public boolean hasStrongType() {
		return left.hasStrongType();
	}
}
