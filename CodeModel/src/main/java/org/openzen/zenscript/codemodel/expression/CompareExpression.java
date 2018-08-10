/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 * Compare expression for basic types. Left and right MUST be of the same type,
 * and MUST be a basic type. (any integer or floating point type, char, string)
 * 
 * @author Hoofdgebruiker
 */
public class CompareExpression extends Expression {
	public final Expression left;
	public final Expression right;
	public final FunctionalMemberRef operator;
	public final CompareType comparison;
	
	public CompareExpression(CodePosition position, Expression left, Expression right, FunctionalMemberRef operator, CompareType comparison, TypeScope scope) {
		super(position, BasicTypeID.BOOL, binaryThrow(position, left.thrownType, right.thrownType));
		
		this.left = left;
		this.right = scope == null ? right : right.castImplicit(position, scope, operator.getHeader().parameters[0].type);
		this.operator = operator;
		this.comparison = comparison;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCompare(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tLeft = left.transform(transformer);
		Expression tRight = right.transform(transformer);
		return left == tLeft && right == tRight ? this : new CompareExpression(position, tLeft, tRight, operator, comparison, null);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		return new CompareExpression(position, left.normalize(scope), right.normalize(scope), operator, comparison, scope);
	}
}
