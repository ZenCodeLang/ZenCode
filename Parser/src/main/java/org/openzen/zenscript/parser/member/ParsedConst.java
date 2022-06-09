package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.CastedEval;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedConst extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final String name;
	private final IParsedType type;
	private final CompilableExpression expression;

	private boolean isCompiled = false;
	private ConstMember compiled;

	public ParsedConst(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			CompilableExpression expression) {
		super(annotations);

		this.position = position;
		this.modifiers = modifiers;
		this.name = name;
		this.type = type;
		this.expression = expression;
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		compiled = new ConstMember(
				position,
				definition,
				modifiers,
				name,
				type.compile(context),
				null);
	}

	@Override
	public ConstMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(MemberCompiler compiler) throws CompileException {
		if (isCompiled)
			return;
		isCompiled = true;

		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, compiler);

		if (expression != null) {
			ExpressionCompiler constCompiler = compiler.forFieldInitializers();
			Expression initializer = expression
					.compile(constCompiler)
					.cast(CastedEval.implicit(constCompiler, position, compiled.getType()))
					.value;
			compiled.value = initializer;

			if (compiled.getType() == BasicTypeID.UNDETERMINED)
				compiled.setType(initializer.type);
		} else if (compiled.getType() == BasicTypeID.UNDETERMINED) {
			throw new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "No type or initializer given");
		}
	}
}
