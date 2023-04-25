package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedLambdaFunctionBody implements ParsedFunctionBody {
	private final CodePosition position;
	private final CompilableExpression value;

	public ParsedLambdaFunctionBody(CodePosition position, CompilableExpression value) {
		this.position = position;
		this.value = value;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		TypeID returnType = compiler.getFunctionHeader().map(FunctionHeader::getReturnType).orElse(BasicTypeID.VOID);
		if (returnType == BasicTypeID.VOID) {
			Expression value = compiler.compile(this.value);
			return new ExpressionStatement(value.position, value);
		} else {
			Expression returnValue = compiler.compile(value, returnType);
			return new ReturnStatement(position, returnValue);
		}
	}
}
