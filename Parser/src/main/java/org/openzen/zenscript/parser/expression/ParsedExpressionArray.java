/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;

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
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		TypeID asBaseType = null;
		TypeID asType = null;
		boolean couldHintType = false;
		
		for (TypeID hint : scope.hints) {
			// TODO: what if multiple hints fit?
			if (hint instanceof ArrayTypeID) {
				ArrayTypeID arrayHint = (ArrayTypeID) hint;
				if (arrayHint.dimension == 1) {
					asBaseType = arrayHint.elementType;
					asType = hint;
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
			throw new CompileException(position, CompileExceptionCode.UNTYPED_EMPTY_ARRAY, "Empty array with unknown type");
		} else {
			ExpressionScope contentScope = scope.withoutHints();
			TypeID resultType = null;
			for (int i = 0; i < contents.size(); i++) {
				cContents[i] = contents.get(i).compileKey(contentScope).eval();
				TypeID joinedType = resultType == null ? cContents[i].type : scope.getTypeMembers(resultType).union(cContents[i].type);
				if (joinedType == null)
					throw new CompileException(position, CompileExceptionCode.TYPE_CANNOT_UNITE, "Could not combine " + resultType + " with " + cContents[i].type);
				
				resultType = joinedType;
			}
			for (int i = 0; i < contents.size(); i++)
				cContents[i] = cContents[i].castImplicit(position, scope, resultType);
			asType = scope.getTypeRegistry().getArray(resultType, 1);
		}
		return new ArrayExpression(position, cContents, asType);
	}

	@Override
	public Expression compileKey(ExpressionScope scope) throws CompileException {
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
