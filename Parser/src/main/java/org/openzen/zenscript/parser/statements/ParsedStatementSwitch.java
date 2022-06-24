package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.List;

public class ParsedStatementSwitch extends ParsedStatement {
	private final String name;
	private final CompilableExpression value;
	private final List<ParsedSwitchCase> cases;

	public ParsedStatementSwitch(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name, CompilableExpression value, List<ParsedSwitchCase> cases) {
		super(position, annotations, whitespace);

		this.name = name;
		this.value = value;
		this.cases = cases;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		SwitchStatement result = new SwitchStatement(position, name, compiler.compile(value));
		StatementCompiler innerScope = compiler.forSwitch(result);

		for (ParsedSwitchCase switchCase : cases) {
			result.cases.add(switchCase.compile(result.value.type, innerScope));
		}

		return result;
	}
}
