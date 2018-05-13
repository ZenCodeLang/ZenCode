/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionArray extends ParsedExpression {
	private final List<ParsedExpression> contents;

	public ParsedExpressionArray(CodePosition position, List<ParsedExpression> contents) {
		super(position);

		this.contents = contents;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID asBaseType = BasicTypeID.ANY;
		ArrayTypeID asType = scope.getTypeRegistry().getArray(asBaseType, 1);
		boolean couldHintType = false;
		
		for (ITypeID hint : scope.hints) {
			// TODO: what if multiple hints fit?
			if (hint.getUnmodified() instanceof ArrayTypeID) {
				ArrayTypeID arrayHint = (ArrayTypeID) hint.getUnmodified();
				if (arrayHint.dimension == 1) {
					asBaseType = arrayHint.elementType;
					asType = arrayHint;
					couldHintType = true;
				}
			}
		}
		
		Expression[] cContents = new Expression[contents.size()];
		if (couldHintType) {
			ExpressionScope contentScope = scope.withHint(asBaseType);
			for (int i = 0; i < contents.size(); i++)
				cContents[i] = contents.get(i).compile(contentScope).eval().castImplicit(position, scope, asBaseType);
		} else if (contents.isEmpty()) {
			return new ArrayExpression(position, cContents, asType);
		} else {
			ExpressionScope contentScope = scope.withoutHints();
			ITypeID resultType = null;
			for (int i = 0; i < contents.size(); i++) {
				cContents[i] = contents.get(i).compileKey(contentScope).eval();
				resultType = resultType == null ? cContents[i].type : scope.getTypeMembers(resultType).union(cContents[i].type);
			}
			for (int i = 0; i < contents.size(); i++)
				cContents[i] = cContents[i].castImplicit(position, scope, resultType);
			asType = scope.getTypeRegistry().getArray(resultType, 1);
		}
		return new ArrayExpression(position, cContents, asType);
	}

	@Override
	public Expression compileKey(ExpressionScope scope) {
		if (contents.size() == 1) {
			return contents.get(0).compile(scope).eval();
		} else {
			return compile(scope).eval();
		}
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
