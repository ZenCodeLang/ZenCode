package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParsedImplementation extends ParsedDefinitionMember {
	private final CodePosition position;
	private final Modifiers modifiers;
	private final IParsedType type;
	private final List<ParsedDefinitionMember> members = new ArrayList<>();


	public ParsedImplementation(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			IParsedType type) {
		super(annotations);

		this.position = position;
		this.modifiers = modifiers;
		this.type = type;
	}

	public void addMember(ParsedDefinitionMember member) {
		members.add(member);
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(definition, compiler);
	}

	private class Compiling implements CompilingMember {
		private final HighLevelDefinition definition;
		private final MemberCompiler compiler;
		private ImplementationMember compiled;
		private List<CompilingMember> members;

		public Compiling(HighLevelDefinition definition, MemberCompiler compiler) {
			this.definition = definition;
			this.compiler = compiler;
		}

		@Override
		public void linkTypes() {
			compiled = new ImplementationMember(position, definition, modifiers, type.compile(compiler.types()));
			members = ParsedImplementation.this.members.stream()
					.map(member -> member.compile(definition, compiled, compiler))
					.collect(Collectors.toList());

			for (CompilingMember member : members) {
				member.linkTypes();
				compiled.addMember(member.getCompiled());
			}
		}

		@Override
		public IDefinitionMember getCompiled() {
			return compiled;
		}

		@Override
		public void prepare(List<CompileException> errors) {
			for (CompilingMember member : members) {
				member.prepare(errors);
			}
		}

		@Override
		public void compile(List<CompileException> errors) {
			for (CompilingMember member : members) {
				member.compile(errors);
			}
		}
	}
}
