package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingSwitchValue;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;

public class ParsedSwitchCase {
	public final CompilableExpression value; // null for default
	public final List<ParsedStatement> statements = new ArrayList<>();

	public ParsedSwitchCase(CompilableExpression value) {
		this.value = value;
	}

	public Compiling compile(StatementCompiler compiler, CodeBlock lastBlock) {
		Compiling result = new Compiling(value.compileSwitchValue(compiler.expressions()));
		for (ParsedStatement statement : statements) {
			CompilingStatement compilingStatement = statement.compile(compiler, lastBlock);
			result.statements.add(compilingStatement);
			lastBlock = compilingStatement.getTail();
		}
		return result;
	}

	public static class Compiling {
		private final CompilingSwitchValue value;
		private final List<CompilingStatement> statements = new ArrayList<>();

		Compiling(CompilingSwitchValue value) {
			this.value = value;
		}

		SwitchCase complete(TypeID type) {
			SwitchValue cValue = value == null ? null : value.as(type);
			Statement[] cStatements = new Statement[statements.size()];
			int i = 0;
			for (CompilingStatement statement : statements) {
				Statement cStatement = statement.complete();
				cStatements[i++] = cStatement;
			}
			return new SwitchCase(cValue, cStatements);
		}
	}
}
