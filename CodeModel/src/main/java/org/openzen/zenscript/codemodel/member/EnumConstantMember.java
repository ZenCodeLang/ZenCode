package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.EnumConstantExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class EnumConstantMember implements CompilableExpression {
	public final CodePosition position;
	public final HighLevelDefinition definition;
	public final String name;
	public final int ordinal;

	public Expression value = null;
	public NewExpression constructor = null;

	public EnumConstantMember(CodePosition position, HighLevelDefinition definition, String name, int ordinal) {
		this.position = position;
		this.definition = definition;
		this.name = name;
		this.ordinal = ordinal;
	}

	@Override
	public CodePosition getPosition() {
		return position;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, this);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final EnumConstantMember member;

		public Compiling(ExpressionCompiler compiler, CodePosition position, EnumConstantMember member) {
			super(compiler, position);

			this.member = member;
		}

		@Override
		public Expression eval() {
			return new EnumConstantExpression(position, DefinitionTypeID.create(member.definition, TypeID.NONE), member);
		}
	}
}
