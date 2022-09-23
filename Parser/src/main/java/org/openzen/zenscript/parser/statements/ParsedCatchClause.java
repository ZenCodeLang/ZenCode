package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedCatchClause {
	public final CodePosition position;
	public final String exceptionName;
	public final ParsedStatement content;

	public ParsedCatchClause(CodePosition position, String exceptionName, ParsedStatement content) {
		this.position = position;
		this.exceptionName = exceptionName;
		this.content = content;
	}

	public CatchClause compile(StatementCompiler compiler) {
		TypeID exceptionType = compiler.expressions().getThrowableType().orElse(BasicTypeID.INVALID);
		VarStatement exceptionVariable = new VarStatement(position, new VariableID(), exceptionName, exceptionType, null, true);
		return new CatchClause(position, exceptionVariable, content.compile(compiler.forCatch(exceptionVariable)));
	}
}
