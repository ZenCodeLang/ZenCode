package org.openzen.zenscript.formattershared;

import org.openzen.zenscript.codemodel.statement.Statement;

import java.util.List;

public class StatementFormattingSubBlock {
	public final String header;
	public final List<String> literalStatements;
	public final Statement[] statements;

	public StatementFormattingSubBlock(String header, List<String> literalStatements, Statement[] statements) {
		this.header = header;
		this.literalStatements = literalStatements;
		this.statements = statements;
	}
}
