/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.ConditionalExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionConditional extends ParsedExpression {
	private final ParsedExpression condition;
	private final ParsedExpression ifThen;
	private final ParsedExpression ifElse;

	public ParsedExpressionConditional(CodePosition position, ParsedExpression condition, ParsedExpression ifThen, ParsedExpression ifElse) {
		super(position);

		this.condition = condition;
		this.ifThen = ifThen;
		this.ifElse = ifElse;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cIfThen = ifThen.compile(scope).eval();
		Expression cIfElse = ifElse.compile(scope).eval();
		
		TypeMembers thenMembers = scope.getTypeMembers(cIfThen.getType());
		TypeMembers elseMembers = scope.getTypeMembers(cIfElse.getType());
		ITypeID resultType = null;
		for (ITypeID hint : scope.hints) {
			if (thenMembers.canCastImplicit(hint) && elseMembers.canCastImplicit(hint)) {
				if (resultType != null)
					throw new CompileException(position, CompileExceptionCode.MULTIPLE_MATCHING_HINTS, "Not sure which type to use");
				
				resultType = hint;
			}
		}
		
		if (resultType == null)
			resultType = thenMembers.union(cIfElse.getType());
		
		if (resultType == null)
			throw new CompileException(position, CompileExceptionCode.TYPE_CANNOT_UNITE, "These types could not be unified: " + cIfThen.getType() + " and " + cIfElse.getType());
		
		cIfThen = cIfThen.castImplicit(position, scope, resultType);
		cIfElse = cIfElse.castImplicit(position, scope, resultType);
		
		return new ConditionalExpression(
				position,
				condition.compile(scope.withHints(BasicTypeID.HINT_BOOL)).eval().castImplicit(position, scope, BasicTypeID.BOOL),
				cIfThen,
				cIfElse,
				resultType);
	}

	@Override
	public boolean hasStrongType() {
		return ifThen.hasStrongType() && ifElse.hasStrongType();
	}
}
