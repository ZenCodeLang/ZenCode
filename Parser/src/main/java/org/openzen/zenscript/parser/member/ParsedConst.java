package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedConst extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final String name;
	private final IParsedType type;
	private final ParsedExpression expression;

	private boolean isCompiled = false;
	private ConstMember compiled;

	public ParsedConst(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			ParsedExpression expression) {
		super(definition, annotations);

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
	public void compile(BaseScope scope) throws CompileException {
		if (isCompiled)
			return;
		isCompiled = true;

		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, scope);

		if (expression != null) {
			Expression initializer = expression
					.compile(new ExpressionScope(scope, compiled.getType()))
					.eval()
					.castImplicit(position, scope, compiled.getType());
			compiled.value = initializer;

			if (compiled.getType() == BasicTypeID.UNDETERMINED)
				compiled.setType(initializer.type);
		} else if (compiled.getType() == BasicTypeID.UNDETERMINED) {
			throw new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "No type or initializer given");
		}
	}
}
