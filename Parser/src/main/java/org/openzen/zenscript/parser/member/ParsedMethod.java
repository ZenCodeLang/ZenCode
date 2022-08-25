package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

public class ParsedMethod extends ParsedFunctionalMember {
	private final String name;
	private final ParsedFunctionHeader header;

	public ParsedMethod(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			String name,
			ParsedFunctionHeader header,
			ParsedFunctionBody body) {
		super(position, modifiers, annotations, body);

		this.name = name;
		this.header = header;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(compiler, definition, implementation);
	}

	private class Compiling extends BaseCompiling<MethodMember> {
		public Compiling(MemberCompiler compiler, HighLevelDefinition definition, ImplementationMember implementation) {
			super(compiler, definition, implementation);
		}

		@Override
		public void linkTypes() {
			compiled = new MethodMember(position, definition, modifiers, name, header.compile(compiler.types()));
		}

		@Override
		protected void fillOverride(TypeID baseType) {
			if (!modifiers.isStatic()) {
				compiler.resolve(baseType)
						.findMethod(name)
						.flatMap(operator -> operator.findOverriddenMethod(compiler, compiled.header))
						.ifPresent(compiled::setOverrides);
			}
		}
	}
}
