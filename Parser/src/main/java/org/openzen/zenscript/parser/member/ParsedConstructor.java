package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

import java.util.List;

public class ParsedConstructor extends ParsedFunctionalMember {
	private final ParsedFunctionHeader header;

	public ParsedConstructor(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations, ParsedFunctionHeader header, ParsedFunctionBody body) {
		super(position, modifiers, annotations, body);

		this.header = header;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(compiler, definition, implementation);
	}

	private class Compiling extends BaseCompiling<ConstructorMember> {
		public Compiling(MemberCompiler compiler, HighLevelDefinition definition, ImplementationMember implementation) {
			super(compiler, definition, implementation);
		}

		@Override
		public void linkTypes() {
			compiled = new ConstructorMember(position, definition, modifiers, header.compile(compiler.types()));
		}

		@Override
		public void compile(List<CompileException> errors) {
			compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, compiler);

			// Implicit .ctors return the created object, but "normal" ones don't, so for non-implict .ctors we change the header to return VOID
			FunctionHeader compilingHeader = modifiers.isImplicit() ? compiled.header : compiled.header.withReturnType(BasicTypeID.VOID);
			compiled.setBody(body.compile(compiler.forMethod(compilingHeader)));

		}

		@Override
		protected void fillOverride(TypeID baseType) {}
	}
}
