package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedCatchClause {
	public final CodePosition position;
	public final String exceptionName;
	public final IParsedType exceptionType;
	public final ParsedStatement content;

	public ParsedCatchClause(CodePosition position, String exceptionName, IParsedType exceptionType, ParsedStatement content) {
		this.position = position;
		this.exceptionName = exceptionName;
		this.exceptionType = exceptionType;
		this.content = content;
	}

	public CatchClause compile(StatementCompiler compiler) {
		VarStatement exceptionVariable = new VarStatement(position, new VariableID(), exceptionName, exceptionType.compile(compiler.types()), null, true);
		return new CatchClause(position, exceptionVariable, content.compile(compiler.forCatch(exceptionVariable)));
	}
}
