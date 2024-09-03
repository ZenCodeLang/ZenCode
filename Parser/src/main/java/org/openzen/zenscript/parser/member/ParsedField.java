package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.List;

public class ParsedField extends ParsedDefinitionMember {
	private final CodePosition position;
	private final Modifiers modifiers;
	private final String name;
	private final IParsedType type;
	private final CompilableExpression expression;
	private final boolean isFinal;

	private final Modifiers autoGetter;
	private final Modifiers autoSetter;

	public ParsedField(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			CompilableExpression expression,
			boolean isFinal,
			Modifiers autoGetter,
			Modifiers autoSetter) {
		super(annotations);

		this.position = position;
		this.modifiers = modifiers;
		this.name = name;
		this.type = type;
		this.expression = expression;
		this.isFinal = isFinal;
		this.autoGetter = autoGetter;
		this.autoSetter = autoSetter;
	}

	@Override
	public CompilingMember compile(HighLevelDefinition definition, ImplementationMember implementation, MemberCompiler compiler) {
		return new Compiling(compiler, definition, implementation);
	}

	private class Compiling implements CompilingMember {
		protected final MemberCompiler compiler;
		protected final HighLevelDefinition definition;
		protected final ImplementationMember implementation;
		private FieldMember compiled;

		public Compiling(MemberCompiler compiler, HighLevelDefinition definition, ImplementationMember implementation) {
			this.compiler = compiler;
			this.definition = definition;
			this.implementation = implementation;
		}

		@Override
		public void linkTypes() {
			compiled = new FieldMember(
					position,
					definition,
					isFinal ? modifiers.withFinal() : modifiers,
					name,
					compiler.getThisType().getThisType(),
					type.compile(compiler.types()),
					autoGetter,
					autoSetter);

			if (expression != null) {
				ExpressionCompiler initializerCompiler = compiler.forFieldInitializers();
				if (compiled.getType() == BasicTypeID.UNDETERMINED) {
					Expression initializer = expression.compile(initializerCompiler).eval();
					compiled.setInitializer(initializer);
					compiled.setType(initializer.type);
				} else {
					Expression initializer = expression
							.compile(initializerCompiler)
							.cast(CastedEval.implicit(initializerCompiler, position, compiled.getType()))
							.value;
					compiled.setInitializer(initializer);
				}
			}
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

			if (compiled.getType() == BasicTypeID.UNDETERMINED) {
				errors.add(new CompileException(position, CompileErrors.fieldWithoutType()));
			}
		}
	}
}
