package org.openzen.zenscript.compiler.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.types.ResolvedType;
import org.openzen.zenscript.compiler.TypeBuilder;

import java.util.List;
import java.util.Optional;

public interface ExpressionCompiler {
	ExpressionBuilder at(CodePosition position, TypeID returnType);

	Optional<TypeID> getThisType();

	TypeBuilder types();

	ResolvedType resolve(TypeID type);

	Optional<CompilingExpression> resolve(GenericName name);

	List<String> findCandidateImports(String name);

	Optional<TypeID> union(TypeID left, TypeID right);

	TypeMatch matchType(TypeID value, TypeID result);

	TypeMatch matchHeader(FunctionHeader available, FunctionHeader requested);
}
