package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;

public class ParsedExpressionArray extends ParsedExpression {
	public final List<CompilableExpression> contents;

	public ParsedExpressionArray(CodePosition position, List<CompilableExpression> contents) {
		super(position);

		this.contents = contents;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression[] elements = contents.stream()
				.map(element -> element.compile(compiler))
				.toArray(CompilingExpression[]::new);

		return new Compiling(compiler, position, elements);
	}

	@Override
	public CompilingExpression compileKey(ExpressionCompiler compiler) {
		if (contents.size() == 1) {
			return contents.get(0).compile(compiler);
		} else {
			return compile(compiler);
		}
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression[] elements;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression[] elements) {
			super(compiler, position);
			this.elements = elements;
		}

		@Override
		public Expression eval() {
			if (elements.length == 0)
				return compiler.at(position).invalid(
						CompileErrors.cannotInfer(),
						compiler.types().arrayOf(BasicTypeID.UNDETERMINED));

			Expression[] elements = new Expression[this.elements.length];
			elements[0] = this.elements[0].eval();
			TypeID elementType = elements[0].type;
			for (int i = 1; i < elements.length; i++) {
				elements[i] = this.elements[i].eval();
				Optional<TypeID> maybeJoinedType = compiler.union(elementType, elements[i].type);
				if (!maybeJoinedType.isPresent())
					return compiler.at(position).invalid(
							CompileErrors.noIntersectionBetweenTypes(elementType, elements[i].type),
							compiler.types().arrayOf(BasicTypeID.UNDETERMINED));

				elementType = maybeJoinedType.get();
			}

			ArrayTypeID type = compiler.types().arrayOf(elementType);
			CastedEval cast = new CastedEval(compiler, position, type.elementType, false, false);
			return compiler.at(position).newArray(type, Arrays.stream(elements)
					.map(e -> cast.of(e).value)
					.toArray(Expression[]::new));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			Optional<ArrayTypeID> maybeArray = cast.type.simplified().asArray()
					.filter(array -> array.dimension == 1);
			if (!maybeArray.isPresent()) {
				CastedExpression castedExpression = cast.of(eval());
				if (!castedExpression.isFailed()) {
					return castedExpression;
				}
				return cast.invalid(CompileErrors.invalidArrayType(cast.type));
			}

			TypeID elementType = maybeArray.get().elementType;
			Expression[] elements = new Expression[this.elements.length];
			CastedExpression.Level level = CastedExpression.Level.EXACT;
			for (int i = 0; i < elements.length; i++) {
				CastedExpression casted = this.elements[i].cast(cast(elementType));
				if (casted.isFailed())
					return casted;
				elements[i] = casted.value;
				level = level.max(casted.level);
			}
			return cast.of(level, compiler.at(position).newArray(maybeArray.get(), elements));
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			for (CompilingExpression element : elements) {
				element.collect(collector);
			}
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			for (CompilingExpression element : elements) {
				element.linkVariables(linker);
			}
		}
	}
}
