package org.openzen.zenscript.rustsource.statements;

import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.rustsource.compiler.ImportSet;
import org.openzen.zenscript.rustsource.expressions.RustExpressionCompiler;
import org.openzen.zenscript.rustsource.types.RustTypeCompiler;

public class RustStatementCompiler implements StatementVisitorWithContext<String, String> {
	private final RustExpressionCompiler expressionCompiler;
	private final RustTypeCompiler typeCompiler;

	public RustStatementCompiler(ImportSet imports, boolean multithreaded) {
		expressionCompiler = new RustExpressionCompiler(imports, multithreaded);
		typeCompiler = new RustTypeCompiler(imports, multithreaded);
	}

	@Override
	public String visitBlock(String indent, BlockStatement statement) {
		StringBuilder result = new StringBuilder();
		result.append("{\n");
		for (Statement line : statement.statements) {
			result.append(line.accept(indent + "  ", this));
		}
		result.append(indent).append("}\n");
		return result.toString();
	}

	@Override
	public String visitBreak(String indent, BreakStatement statement) {
		return indent + "break;";
	}

	@Override
	public String visitContinue(String indent, ContinueStatement statement) {
		return indent + "continue;";
	}

	@Override
	public String visitDoWhile(String indent, DoWhileStatement statement) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public String visitEmpty(String indent, EmptyStatement statement) {
		return ";";
	}

	@Override
	public String visitExpression(String indent, ExpressionStatement statement) {
		return indent + expressionCompiler.compile(statement.expression) + ";";
	}

	@Override
	public String visitForeach(String indent, ForeachStatement statement) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public String visitIf(String indent, IfStatement statement) {
		StringBuilder result = new StringBuilder();
		result.append(indent).append("if ").append(expressionCompiler.compile(statement.condition)).append(" {\n");
		statement.onThen.accept(indent + "  ", this);
		if (statement.onElse != null) {
			result.append(indent).append("} else {\n");
			statement.onElse.accept(indent + "  ", this);
			result.append(indent).append("}\n");
		}
		return result.toString();
	}

	@Override
	public String visitLock(String indent, LockStatement statement) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public String visitReturn(String indent, ReturnStatement statement) {
		// NOTE: can be compiled differently if it's the last statement
		if (statement.value == null) {
			return indent + "return;";
		} else {
			return indent + "return " + expressionCompiler.compile(statement.value) + ";";
		}
	}

	@Override
	public String visitSwitch(String indent, SwitchStatement statement) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public String visitThrow(String indent, ThrowStatement statement) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public String visitTryCatch(String indent, TryCatchStatement statement) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	@Override
	public String visitVar(String indent, VarStatement statement) {
		StringBuilder result = new StringBuilder();
		result.append(indent).append("let ");
		if (!statement.isFinal)
			result.append("mut ");
		result.append(statement.name);
		if (statement.initializer != null) {
			result.append(" = ");
			result.append(expressionCompiler.compile(statement.initializer));
		} else if (statement.type != null) {
			result.append(": ");
			result.append(typeCompiler.compile(statement.type));
		}
		result.append(";\n");
		return result.toString();
	}

	@Override
	public String visitWhile(String indent, WhileStatement statement) {
		throw new UnsupportedOperationException("Not yet supported");
	}
}
