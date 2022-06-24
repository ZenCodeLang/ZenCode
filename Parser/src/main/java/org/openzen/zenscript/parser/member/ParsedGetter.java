package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.List;

public class ParsedGetter extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedFunctionBody body;

	private final String name;
	private final IParsedType type;

	public ParsedGetter(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			ParsedFunctionBody body) {
		super(annotations);

		this.position = position;
		this.modifiers = modifiers;
		this.body = body;

		this.name = name;
		this.type = type;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(definition, implementation, compiler);
	}

	private class Compiling implements CompilingMember {
		private final HighLevelDefinition definition;
		private final ImplementationMember implementation;
		private final MemberCompiler compiler;
		private GetterMember compiled;

		public Compiling(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
			this.definition = definition;
			this.implementation = implementation;
			this.compiler = compiler;
		}

		@Override
		public void linkTypes() {
			compiled = new GetterMember(position, definition, modifiers, name, type.compile(compiler.types()), null);
		}

		@Override
		public void prepare(List<CompileException> errors) {
			inferHeaders(errors);
		}

		@Override
		public void compile(List<CompileException> errors) {
			FunctionHeader header = new FunctionHeader(compiled.getType());
			compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, compiler);
			StatementCompiler statementCompiler = compiler.forMethod(header);
			compiled.setBody(body.compile(statementCompiler));
		}

		private void inferHeaders(List<CompileException> errors) {
			if ((implementation != null && !Modifiers.isPrivate(modifiers))) {
				fillOverride(implementation.type);
			} else if (implementation == null && Modifiers.isOverride(modifiers)) {
				if (definition.getSuperType() == null)
					errors.add(new CompileException(position, CompileExceptionCode.OVERRIDE_WITHOUT_BASE, "Override specified without base type"));
				else
					fillOverride(definition.getSuperType());
			}

			if (compiled == null)
				throw new IllegalStateException("Types not yet linked");
		}

		private void fillOverride(TypeID baseType) {
			ResolvedType resolved = compiler.resolve(baseType);
			resolved.findGetter(name).ifPresent(getter -> compiled.setOverrides(getter));
		}
	}
}
