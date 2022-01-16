package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ParsedExpressionArray extends ParsedExpression {

	public static final List<BiFunction<ParsedExpressionArray, ExpressionScope, IPartialExpression>> compileOverrides = new ArrayList<>(0);
	public final List<ParsedExpression> contents;

	public ParsedExpressionArray(CodePosition position, List<ParsedExpression> contents) {
		super(position);

		this.contents = contents;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {

		for (BiFunction<ParsedExpressionArray, ExpressionScope, IPartialExpression> compileOverride : compileOverrides) {
			final IPartialExpression apply = compileOverride.apply(this, scope);
			if (apply != null)
				return apply;
		}

		TypeID asBaseType;
		TypeID asType = null;
		Expression[] compiledContents = new Expression[contents.size()];

		outer:
		for (TypeID hint : scope.hints) {
			ArrayTypeID arrayHint = null;
			if (hint instanceof ArrayTypeID) {
				arrayHint = (ArrayTypeID) hint;
				asType = hint;
			}
			if (hint.isOptional() && hint.withoutOptional() instanceof ArrayTypeID) {
				arrayHint = (ArrayTypeID) hint.withoutOptional();
				asType = hint.withoutOptional();
			}
			if (arrayHint != null && arrayHint.dimension == 1) {
				asBaseType = arrayHint.elementType;

				ExpressionScope contentScope = scope.withHint(asBaseType);
				for (int i = 0; i < contents.size(); i++) {
					Expression expression = contents.get(i).compile(contentScope).eval().castImplicit(position, scope, asBaseType);
					if (expression instanceof InvalidExpression) {
						continue outer;
					}
					compiledContents[i] = expression;
				}
				return new ArrayExpression(position, compiledContents, asType);
			}
		}

		if (contents.isEmpty()) {
			throw new CompileException(position, CompileExceptionCode.UNTYPED_EMPTY_ARRAY, "Empty array with unknown type");
		} else {
			ExpressionScope contentScope = scope.withoutHints();
			TypeID resultType = null;
			for (int i = 0; i < contents.size(); i++) {
				compiledContents[i] = contents.get(i).compile(contentScope).eval();
				TypeID joinedType = resultType == null ? compiledContents[i].type : scope.getTypeMembers(resultType)
						.union(compiledContents[i].type);
				if (joinedType == null) {
					throw new CompileException(position, CompileExceptionCode.TYPE_CANNOT_UNITE, "Could not combine " + resultType + " with " + compiledContents[i].type);
				}

				resultType = joinedType;
			}
			for (int i = 0; i < contents.size(); i++) {
				compiledContents[i] = compiledContents[i].castImplicit(position, scope, resultType);
			}
			asType = scope.getTypeRegistry().getArray(resultType, 1);
		}
		return new ArrayExpression(position, compiledContents, asType);
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
