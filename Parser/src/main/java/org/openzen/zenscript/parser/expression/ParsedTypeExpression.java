package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.StaticMemberCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.Optional;

public class ParsedTypeExpression extends ParsedExpression {
	private final IParsedType type;

	public ParsedTypeExpression(CodePosition position, IParsedType type) {
		super(position);

		this.type = type;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		TypeID type = this.type.compile(compiler.types());
		return new Compiling(compiler, position, type);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final TypeID type;

		public Compiling(ExpressionCompiler compiler, CodePosition position, TypeID type) {
			super(compiler, position);
			this.type = type;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).invalid(CompileErrors.cannotUseTypeAsValue());
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			Optional<TypeSymbol> maybeInnerType = compiler.resolve(type).findInnerType(name.name);
			if (maybeInnerType.isPresent())
				return new Compiling(compiler, position, compiler.types().definitionOf(maybeInnerType.get(), name.arguments));

			return new StaticMemberCompilingExpression(compiler, position, type, name);
		}
	}
}
