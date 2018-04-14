/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class RangeExpression extends Expression {
	public final Expression from;
	public final Expression to;
	
	public RangeExpression(CodePosition position, GlobalTypeRegistry registry, Expression from, Expression to) {
		super(position, registry.getRange(from.getType(), to.getType()));
	
		this.from = from;
		this.to = to;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitRange(this);
	}
}
