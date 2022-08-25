package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;

import java.util.List;
import java.util.Optional;

public class ParsedInnerDefinition extends ParsedDefinitionMember {
	private final ParsedDefinition innerDefinition;

	public ParsedInnerDefinition(ParsedDefinition definition) {
		super(ParsedAnnotation.NONE);

		this.innerDefinition = definition;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(compiler, definition);
	}

	private class Compiling implements CompilingMember {
		private final CompilingDefinition innerDefinition;
		private final InnerDefinitionMember member;

		public Compiling(MemberCompiler compiler, HighLevelDefinition definition) {
			innerDefinition = ParsedInnerDefinition.this.innerDefinition.compileAsDefinition(compiler.forInner(), definition);
			member = new InnerDefinitionMember(definition.position, definition, definition.modifiers, innerDefinition.getDefinition());
		}

		@Override
		public void linkTypes() {
			innerDefinition.linkTypes();
		}

		@Override
		public IDefinitionMember getCompiled() {
			return member;
		}

		@Override
		public void prepare(List<CompileException> errors) {
			innerDefinition.prepareMembers(errors);
		}

		@Override
		public void compile(List<CompileException> errors) {
			innerDefinition.compileMembers(errors);
		}

		@Override
		public Optional<CompilingDefinition> asInner() {
			return Optional.of(innerDefinition);
		}
	}
}
