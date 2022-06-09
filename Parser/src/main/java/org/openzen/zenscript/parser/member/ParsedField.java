package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedField extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final String name;
	private final IParsedType type;
	private final CompilableExpression expression;
	private final boolean isFinal;

	private final int autoGetter;
	private final int autoSetter;

	private FieldMember compiled;
	private boolean isCompiled = false;

	public ParsedField(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			CompilableExpression expression,
			boolean isFinal,
			int autoGetter,
			int autoSetter) {
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
	public void linkTypes(TypeResolutionContext context) {
		compiled = new FieldMember(
				position,
				definition,
				modifiers | (isFinal ? Modifiers.FINAL : 0),
				name,
				context.getThisType(),
				type.compile(context),
				context.getTypeRegistry(),
				autoGetter,
				autoSetter,
				null);
	}

	@Override
	public FieldMember getCompiled() {
		return compiled;
	}

	@Override
	public CompilingMember compile(MemberCompiler compiler) throws CompileException {
		if (isCompiled)
			return;
		isCompiled = true;

		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, compiler);

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
		} else if (compiled.getType() == BasicTypeID.UNDETERMINED) {
			throw new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "Could not infer type since no initializer is given");
		}
	}

	private class Compiling implements CompilingMember {

	}
}
