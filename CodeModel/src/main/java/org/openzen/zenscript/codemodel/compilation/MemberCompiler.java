package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;

public interface MemberCompiler extends TypeResolver {
	LocalType getThisType();

	TypeBuilder types();

	ExpressionCompiler forFieldInitializers();

	StatementCompiler forMethod(FunctionHeader header);

	DefinitionCompiler forInner();
}
