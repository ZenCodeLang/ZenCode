package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;

import java.util.Optional;

public class ParsedInnerDefinition extends ParsedDefinitionMember {
	private final ParsedDefinition innerDefinition;

	public ParsedInnerDefinition(ParsedDefinition definition) {
		super(ParsedAnnotation.NONE);

		this.innerDefinition = definition;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, MemberCompiler compiler) {
		return new Compiling(compiler, definition);
	}

	private class Compiling implements CompilingMember {
		private final MemberCompiler compiler;
		private final HighLevelDefinition definition;
		private final CompilingDefinition innerDefinition;
		private final InnerDefinitionMember member;

		public Compiling(MemberCompiler compiler, HighLevelDefinition definition) {
			this.compiler = compiler;
			this.definition = definition;
			innerDefinition = ParsedInnerDefinition.this.innerDefinition.?;
		}

		@Override
		public void linkTypes() {
			innerDefinition.linkTypes();
		}

		@Override
		public void prepare() {

		}

		@Override
		public void compile() {

		}

		@Override
		public Optional<CompilingDefinition> asInner() {
			return Optional.of(definition);
		}
	}
}
