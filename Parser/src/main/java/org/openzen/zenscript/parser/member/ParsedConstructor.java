package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

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
		protected void fillOverride(TypeID baseType) {}
	}
}
