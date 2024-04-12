package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public interface StatementCompiler {
	ExpressionCompiler expressions();

	TypeBuilder types();

	ResolvedType resolve(TypeID type);

	StatementCompiler forBlock();

	StatementCompiler forLoop(CompilingLoopStatement loop);

	StatementCompiler forCatch(CompilingVariable exceptionVariable);

	Optional<CompilingLoopStatement> getLoop(String name);

	Optional<FunctionHeader> getFunctionHeader();

	Optional<TypeID> getThrownType();

	void addLocalVariable(CompilingVariable variable);
}
