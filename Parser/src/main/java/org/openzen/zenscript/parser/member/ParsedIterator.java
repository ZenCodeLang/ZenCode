package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.List;

public class ParsedIterator extends ParsedFunctionalMember {
	private final List<IParsedType> loopVariableTypes;

	public ParsedIterator(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			List<IParsedType> loopVariableTypes,
			ParsedFunctionBody body) {
		super(position, modifiers, annotations, body);
		this.loopVariableTypes = loopVariableTypes;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(definition, implementation, compiler);
	}

	private class Compiling extends BaseCompiling<IteratorMember> {
		public Compiling(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
			super(compiler, definition, implementation);
		}

		@Override
		public void linkTypes() {
			TypeID[] loopVariableTypes = new TypeID[ParsedIterator.this.loopVariableTypes.size()];
			for (int i = 0; i < loopVariableTypes.length; i++)
				loopVariableTypes[i] = ParsedIterator.this.loopVariableTypes.get(i).compile(compiler.types());

			compiled = new IteratorMember(position, definition, modifiers, loopVariableTypes);
		}

		@Override
		protected void fillOverride(TypeID baseType) {
			compiler.resolve(baseType)
					.findIterator(loopVariableTypes.size())
					.flatMap(IteratorInstance::getMethod)
					.ifPresent(compiled::setOverrides);
		}
	}
}
