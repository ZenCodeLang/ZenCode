package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedGetter extends ParsedFunctionalMember {
	private final String name;
	private final IParsedType type;

	public ParsedGetter(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			ParsedFunctionBody body) {
		super(position, modifiers, annotations, body);

		this.name = name;
		this.type = type;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(definition, implementation, compiler);
	}

	private class Compiling extends BaseCompiling<GetterMember> {
		private GetterMember compiled;

		public Compiling(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
			super(compiler, definition, implementation);
		}

		@Override
		public void linkTypes() {
			compiled = new GetterMember(position, definition, modifiers, name, type.compile(compiler.types()));
		}

		@Override
		protected void fillOverride(TypeID baseType) {
			ResolvedType resolved = compiler.resolve(baseType);
			resolved.findGetter(name)
					.flatMap(getter -> getter.findOverriddenMethod(compiler, compiled.header))
					.ifPresent(compiled::setOverrides);
		}
	}
}
