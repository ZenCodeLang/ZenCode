package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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

		boolean couldHintType = false;
		TypeID asBaseType;
		TypeID arrayType;
		Expression[] compiledContents = new Expression[contents.size()];

		List<ArrayTypeID> exactHints = new ArrayList<>();
		List<ArrayTypeID> castedHints = new ArrayList<>();

		for (TypeID hint : scope.hints) {
			ArrayTypeID arrayHint = null;
			if (hint instanceof ArrayTypeID) {
				arrayHint = (ArrayTypeID) hint;
			}
			if (hint.isOptional() && hint.withoutOptional() instanceof ArrayTypeID) {
				arrayHint = (ArrayTypeID) hint.withoutOptional();
			}
			if (arrayHint != null && arrayHint.dimension == 1) {
				asBaseType = arrayHint.elementType;

				ExpressionScope contentScope = scope.withHint(asBaseType);
				boolean isBaseType = true;
				boolean canCastToBase = true;
				for (ParsedExpression content : contents) {
					TypeID type = content.compile(contentScope).eval().type;
					isBaseType &= type == asBaseType;
					canCastToBase &= contentScope.getTypeMembers(type).canCastImplicit(asBaseType);
				}
				if (canCastToBase || isBaseType) {
					couldHintType = true;
					if (isBaseType) {
						exactHints.add(arrayHint);
					}
					castedHints.add(arrayHint);
				}
			}

		}
		if (couldHintType) {
			if (exactHints.isEmpty() && castedHints.isEmpty()) {
				throw new CompileException(position, CompileExceptionCode.INVALID_CAST, "Unable to cast array!");
			}
			if (exactHints.size() > 1) {
				throw new CompileException(position, CompileExceptionCode.CALL_AMBIGUOUS, String.format("Ambiguous call; multiple types can match exactly: %n%s", scope.hints.stream().map(Object::toString).collect(Collectors.joining("\n"))));
			}
			if (castedHints.size() > 1 && exactHints.isEmpty()) {
				throw new CompileException(position, CompileExceptionCode.CALL_AMBIGUOUS, String.format("Ambiguous call; multiple types can cast: %n%s", scope.hints.stream().map(Object::toString).collect(Collectors.joining("\n"))));
			}

			ArrayTypeID foundHint = exactHints.isEmpty() ? castedHints.get(0) : exactHints.get(0);
			arrayType = foundHint;
			asBaseType = foundHint.elementType;
			ExpressionScope compilingScope = scope.withHint(asBaseType);
			for (int i = 0; i < contents.size(); i++) {
				Expression expression = contents.get(i).compile(compilingScope).eval().castImplicit(position, scope, asBaseType);
				compiledContents[i] = expression;
			}

		} else if (contents.isEmpty()) {
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
			arrayType = scope.getTypeRegistry().getArray(resultType, 1);
		}
		return new ArrayExpression(position, compiledContents, arrayType);
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
