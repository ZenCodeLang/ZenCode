package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.List;

public class ParsedSetter extends ParsedFunctionalMember {
	private final String name;
	private final IParsedType type;

	public ParsedSetter(
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

	private class Compiling extends BaseCompiling<SetterMember> {
		public Compiling(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
			super(compiler, definition, implementation);
		}

		@Override
		public void compile(List<CompileException> errors) {
			compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, compiler);

			StatementCompiler bodyCompiler = compiler.forMethod(compiled.header);
			compiled.setBody(body.compile(compiler.forMethod(compiled.header)));
			
		}

		@Override
		public void linkTypes() {
			compiled = new SetterMember(position, definition, modifiers, name, type.compile(compiler.types()));
		}

		@Override
		protected void fillOverride(TypeID baseType) {
			ResolvedType resolved = compiler.resolve(baseType);
			resolved.findSetter(name)
					.flatMap(setter -> setter.findOverriddenMethod(compiler, compiled.header))
					.ifPresent(compiled::setOverrides);
		}
	}
}
