package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

import java.util.List;

public abstract class ParsedFunctionalMember extends ParsedDefinitionMember {
	protected final CodePosition position;
	protected final Modifiers modifiers;
	protected final ParsedFunctionBody body;

	public ParsedFunctionalMember(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			ParsedFunctionBody body) {
		super(annotations);

		this.position = position;
		this.modifiers = modifiers;
		this.body = body;
	}

	protected abstract class BaseCompiling<T extends FunctionalMember> implements CompilingMember {
		protected final MemberCompiler compiler;
		protected final HighLevelDefinition definition;
		protected final ImplementationMember implementation;
		protected T compiled;

		public BaseCompiling(MemberCompiler compiler, HighLevelDefinition definition, ImplementationMember implementation) {
			this.compiler = compiler;
			this.definition = definition;
			this.implementation = implementation;
		}

		@Override
		public IDefinitionMember getCompiled() {
			return compiled;
		}

		@Override
		public void prepare(List<CompileException> errors) {
			inferHeaders(errors);
		}

		@Override
		public void compile(List<CompileException> errors) {
			compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, compiler);
			compiled.setBody(body.compile(compiler.forMethod(compiled.header)));
		}

		private void inferHeaders(List<CompileException> errors) {
			if ((implementation != null && !modifiers.isPrivate())) {
				fillOverride(implementation.type);
			} else if (implementation == null && modifiers.isOverride()) {
				if (definition.getSuperType() == null) {
					errors.add(new CompileException(position, CompileErrors.overrideWithoutBase()));
					return;
				}

				fillOverride(definition.getSuperType());
			}
		}

		protected abstract void fillOverride(TypeID baseType);
	}
}
