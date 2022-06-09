package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.MatchExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;

public class ParsedMatchExpression extends ParsedExpression {
	public final CompilableExpression value;
	public final List<Case> cases;

	public ParsedMatchExpression(CodePosition position, CompilableExpression value, List<Case> cases) {
		super(position);

		this.value = value;
		this.cases = cases;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		Expression value = this.value.compile(compiler).eval();
		CompilingCase[] cCases = new CompilingCase[cases.size()];
		for (int i = 0; i < cases.size(); i++) {
			Case matchCase = cases.get(i);
			cCases[i] = matchCase.compile(value.type, compiler);
		}

		return new Compiling(compiler, position, value, cCases);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final Expression value;
		private final CompilingCase[] cases;

		public Compiling(ExpressionCompiler compiler, CodePosition position, Expression value, CompilingCase[] cases) {
			super(compiler, position);

			this.value = value;
			this.cases = cases;
		}

		@Override
		public Expression eval() {
			if (cases.length == 0)
				return compiler.at(position).invalid(CompileErrors.noMatchValuesForInference());

			Expression[] values = new Expression[cases.length];
			values[0] = cases[0].value.eval();
			TypeID type = values[0].type;

			for (int i = 1; i < values.length; i++) {
				values[i] = cases[i].value.eval();
				Optional<TypeID> union = compiler.union(type, values[i].type);
				if (union.isPresent())
					type = union.get();
				else
					return compiler.at(position).invalid(CompileErrors.noIntersectionBetweenTypes(type, values[i].type));
			}

			MatchExpression.Case[] cases = new MatchExpression.Case[this.cases.length];
			CastedEval cast = cast(type);
			for (int i = 0; i < cases.length; i++) {
				cases[i] = new MatchExpression.Case(cases[i].key, cast.of(values[i]).value);
			}

			return compiler.at(position).match(value, type, cases);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			MatchExpression.Case[] cases = new MatchExpression.Case[this.cases.length];
			for (int i = 0; i < cases.length; i++)
				cases[i] = this.cases[i].cast(cast);

			return cast.of(compiler.at(position).match(value, cast.type, cases));
		}
	}

	private static class CompilingCase {
		public final SwitchValue name;
		public final CompilingExpression value;

		public CompilingCase(SwitchValue name, CompilingExpression value) {
			this.name = name;
			this.value = value;
		}

		public MatchExpression.Case eval() {
			return new MatchExpression.Case(name, value.eval());
		}

		public MatchExpression.Case cast(CastedEval cast) {
			return new MatchExpression.Case(name, value.cast(cast).value);
		}
	}

	public static class Case {
		public final CompilableExpression name;
		public final CompilableExpression value;

		public Case(CompilableExpression name, CompilableExpression body) {
			this.name = name;
			this.value = body;
		}

		public CompilingCase compile(TypeID valueType, ExpressionCompiler compiler) {
			if (name == null)
				return new CompilingCase(null, value.compile(compiler));

			SwitchValue switchValue = name.asSwitchValue(valueType, compiler);
			ExpressionCompiler inner = compiler.withLocalVariables(switchValue.getBindings());
			return new CompilingCase(switchValue, value.compile(inner));
		}
	}
}
