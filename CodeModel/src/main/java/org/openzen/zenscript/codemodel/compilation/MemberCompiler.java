package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface MemberCompiler {
	LocalType getThisType();

	ResolvedType resolve(TypeID type);

	TypeBuilder types();

	ExpressionCompiler forFieldInitializers();

	StatementCompiler forMethod(FunctionHeader header);
}
