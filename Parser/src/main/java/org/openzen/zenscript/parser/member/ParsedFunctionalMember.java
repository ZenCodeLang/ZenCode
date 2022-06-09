package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

public abstract class ParsedFunctionalMember extends ParsedDefinitionMember {
	protected final CodePosition position;
	protected final int modifiers;
	protected final ParsedFunctionBody body;

	public ParsedFunctionalMember(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ParsedFunctionBody body) {
		super(annotations);

		this.position = position;
		this.modifiers = modifiers;
		this.body = body;
	}

	protected void inferHeaders(MemberCompiler compiler) throws CompileException {
		if ((implementation != null && !Modifiers.isPrivate(modifiers))) {
			fillOverride(compiler, implementation.getCompiled().type);
		} else if (implementation == null && Modifiers.isOverride(modifiers)) {
			if (definition.getSuperType() == null)
				throw new CompileException(position, CompileExceptionCode.OVERRIDE_WITHOUT_BASE, "Override specified without base type");

			fillOverride(compiler, definition.getSuperType());
		}

		if (getCompiled() == null || getCompiled().header == null)
			throw new IllegalStateException("Types not yet linked");
	}

	@Override
	public final CompilingMember compile(CompilingDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		inferHeaders(compiler);

		StatementCompiler statementCompiler = compiler.forMethod(getCompiled().header);
		getCompiled().annotations = ParsedAnnotation.compileForMember(annotations, getCompiled(), compiler);
		getCompiled().setBody(body.compile(statementCompiler, getCompiled().header));

		if (getCompiled().header.getReturnType() == BasicTypeID.UNDETERMINED) {
			if (getCompiled().body == null)
				throw new CompileException(position, CompileExceptionCode.CANNOT_INFER_RETURN_TYPE, "Method return type could not be inferred");

			TypeID returnType = getCompiled().body.getReturnType();
			if (returnType == null) {
				throw new CompileException(position, CompileExceptionCode.CANNOT_INFER_RETURN_TYPE, "Method return type could not be inferred");
			} else {
				getCompiled().header.setReturnType(returnType);
			}
		}
	}

	protected abstract void fillOverride(MemberCompiler compiler, TypeID baseType) throws CompileException;
}
