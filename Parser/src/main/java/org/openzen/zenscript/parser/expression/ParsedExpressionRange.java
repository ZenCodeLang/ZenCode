/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.RangeExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionRange extends ParsedExpression {
	private final ParsedExpression from;
	private final ParsedExpression to;
	
	public ParsedExpressionRange(CodePosition position, ParsedExpression from, ParsedExpression to) {
		super(position);
		
		this.from = from;
		this.to = to;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		List<ITypeID> fromHints = new ArrayList<>();
		List<ITypeID> toHints = new ArrayList<>();
		
		for (ITypeID hint : scope.hints) {
			if (hint instanceof RangeTypeID) {
				RangeTypeID rangeHint = (RangeTypeID) hint;
				if (!fromHints.contains(rangeHint.from))
					fromHints.add(rangeHint.from);
				if (!toHints.contains(rangeHint.to))
					toHints.add(rangeHint.to);
			}
		}
		
		Expression from = this.from.compile(scope.withHints(fromHints)).eval();
		Expression to = this.to.compile(scope.withHints(toHints)).eval();
		return new RangeExpression(position, scope.getTypeRegistry(), from, to);
	}

	@Override
	public boolean hasStrongType() {
		return from.hasStrongType() && to.hasStrongType();
	}
}
