package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.AnyMethod;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedCaster extends ParsedFunctionalMember {
	private final IParsedType type;

	public ParsedCaster(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			IParsedType type,
			ParsedFunctionBody body) {
		super(position, modifiers, annotations, body);

		this.type = type;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(compiler, definition, implementation);
	}

	private class Compiling extends BaseCompiling<CasterMember> {
		public Compiling(MemberCompiler compiler, HighLevelDefinition definition, ImplementationMember implementation) {
			super(compiler, definition, implementation);
		}

		@Override
		public void linkTypes() {
			compiled = new CasterMember(position, definition, modifiers, type.compile(compiler.types()));
		}

		@Override
		protected void fillOverride(TypeID baseType) {
			if (!modifiers.isStatic()) {
				compiler.resolve(baseType)
						.findCaster(compiled.toType)
						.flatMap(AnyMethod::asMethod)
						.ifPresent(compiled::setOverrides);
			}
		}
	}
}
