package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingExpressionCodeStatement;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.ssa.SSA;
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
		CodeBlock initialBlock = new CodeBlock();
		CompilingExpression compiling = value.compile(compiler.expressions());
		initialBlock.add(new CompilingExpressionCodeStatement(compiling));

		SSA ssa = new SSA(initialBlock);
		ssa.compute();

		TypeID returnType = compiler.getFunctionHeader().map(FunctionHeader::getReturnType).orElse(BasicTypeID.VOID);
		if (returnType == BasicTypeID.VOID) {
			Expression value = compiling.eval();
			return new ExpressionStatement(value.position, value);
		} else {
			Expression returnValue = compiling.as(returnType);
			return new ReturnStatement(position, returnValue);
		}
	}
}
