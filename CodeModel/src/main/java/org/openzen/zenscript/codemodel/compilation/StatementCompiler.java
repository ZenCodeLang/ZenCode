package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public interface StatementCompiler {
	Expression compile(CompilableExpression expression);

	Expression compile(CompilableExpression expression, TypeID type);

	ExpressionCompiler expressions();

	SwitchValue compileSwitchValue(CompilableExpression expression, TypeID type);

	TypeBuilder types();

	ResolvedType resolve(TypeID type);

	StatementCompiler forBlock();

	StatementCompiler forLoop(LoopStatement loop);

	StatementCompiler forForeach(ForeachStatement statement);

	StatementCompiler forSwitch(SwitchStatement statement);

	StatementCompiler forCatch(VarStatement exceptionVariable);

	Optional<LoopStatement> getLoop(String name);

	Optional<FunctionHeader> getFunctionHeader();

	void addLocalVariable(VarStatement variable);
}
