package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.CompilingVariable;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
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

	public Compiling compile(StatementCompiler compiler, CodeBlock lastBlock) {
		CompilingVariable exceptionVariable = new CompilingVariable(new VariableID(), exceptionName, null, true);
		return new Compiling(compiler, exceptionVariable, content.compile(compiler.forCatch(exceptionVariable), lastBlock));
	}

	public class Compiling {
		private final StatementCompiler compiler;
		private final CompilingVariable exceptionVariable;
		private final CompilingStatement content;


		public Compiling(StatementCompiler compiler, CompilingVariable exceptionVariable, CompilingStatement content) {
			this.compiler = compiler;
			this.exceptionVariable = exceptionVariable;
			this.content = content;
		}

		public CatchClause complete() {
			TypeID exceptionType = compiler.expressions().getThrowableType().orElse(BasicTypeID.INVALID);
			VarStatement exceptionVariable = new VarStatement(position, this.exceptionVariable.id, this.exceptionVariable.name, exceptionType, null, true);
			return new CatchClause(position, exceptionVariable, content.complete());
		}
	}
}
