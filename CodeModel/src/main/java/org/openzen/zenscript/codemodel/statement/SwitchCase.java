package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;

public class SwitchCase {
	public final SwitchValue value;
	public final Statement[] statements;

	public SwitchCase(SwitchValue value, Statement[] statements) {
		this.value = value;
		this.statements = statements;
	}

	public boolean isDefault() {
		return value == null;
	}

	public SwitchCase transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Statement[] tStatements = new Statement[statements.length];
		int i = 0;
		for (Statement statement : statements) {
			Statement transformed = statement.transform(transformer, modified);
			tStatements[i++] = transformed;
		}
		return new SwitchCase(value, tStatements);
	}

	public SwitchCase transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Statement[] tStatements = new Statement[statements.length];
		int i = 0;
		for (Statement statement : statements) {
			Statement transformed = statement.transform(transformer, modified);
			tStatements[i++] = transformed;
		}
		return new SwitchCase(value, tStatements);
	}
}
