package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedStatement;

import java.util.List;

public class ParsedStaticInitializer extends ParsedDefinitionMember {
	private final CodePosition position;
	private final ParsedStatement body;

	public ParsedStaticInitializer(CodePosition position, ParsedAnnotation[] annotations, ParsedStatement body) {
		super(annotations);

		this.position = position;
		this.body = body;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(definition, compiler);
	}

	private class Compiling implements CompilingMember {
		private final HighLevelDefinition definition;
		private final MemberCompiler compiler;
		private StaticInitializerMember compiled;

		public Compiling(HighLevelDefinition definition, MemberCompiler compiler) {
			this.definition = definition;
			this.compiler = compiler;
		}

		@Override
		public void linkTypes() {
			compiled = new StaticInitializerMember(position, definition);
		}

		@Override
		public IDefinitionMember getCompiled() {
			return compiled;
		}

		@Override
		public void prepare(List<CompileException> errors) {

		}

		@Override
		public void compile(List<CompileException> errors) {
			compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, compiler);
			compiled.body = body.compile(compiler.forMethod(new FunctionHeader(BasicTypeID.VOID)), new CodeBlock()).complete();
		}
	}
}
