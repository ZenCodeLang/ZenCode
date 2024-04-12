package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

public class MemberCompilerImpl implements MemberCompiler {
	private final CompileContext context;
	private final LocalType localType;
	private final TypeBuilder types;
	private final DefinitionCompiler definitionCompiler;

	public MemberCompilerImpl(CompileContext context, DefinitionCompiler definitionCompiler, TypeID forType, TypeBuilder types) {
		this.context = context;
		this.definitionCompiler = definitionCompiler;
		this.localType = new LocalTypeImpl(forType, definitionCompiler);
		this.types = types;
	}

	@Override
	public LocalType getThisType() {
		return localType;
	}

	@Override
	public TypeBuilder types() {
		return types;
	}

	@Override
	public ExpressionCompiler forFieldInitializers() {
		return new ExpressionCompilerImpl(context, localType, types, null, LocalSymbols.empty(), FunctionHeader.EMPTY);
	}

	@Override
	public StatementCompiler forMethod(FunctionHeader header) {
		return new StatementCompilerImpl(context, localType, types, header, new LocalSymbols(header), null);
	}

	@Override
	public DefinitionCompiler forInner() {
		return definitionCompiler;
	}

	@Override
	public ResolvedType resolve(TypeID type) {
		return definitionCompiler.resolve(type);
	}
}
