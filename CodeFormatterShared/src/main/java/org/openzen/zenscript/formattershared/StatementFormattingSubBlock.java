package org.openzen.zenscript.formattershared;

import java.util.List;
import org.openzen.zenscript.codemodel.statement.Statement;

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
