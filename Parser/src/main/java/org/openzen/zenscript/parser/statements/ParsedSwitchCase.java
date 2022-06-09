package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
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

	public SwitchCase compile(TypeID type, StatementCompiler compiler) {
		SwitchValue cValue = value == null ? null : compiler.compileSwitchValue(value, type);
		Statement[] cStatements = new Statement[statements.size()];
		int i = 0;
		for (ParsedStatement statement : statements) {
			cStatements[i++] = statement.compile(compiler);
		}
		return new SwitchCase(cValue, cStatements);
	}
}
