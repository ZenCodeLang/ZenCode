package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class CastedEval {
	public static CastedEval implicit(ExpressionCompiler compiler, CodePosition position, TypeID type) {
		return new CastedEval(compiler, position, type, false, false);
	}

	private final ExpressionCompiler compiler;
	private final CodePosition position;
	public final TypeID type;
	private final boolean explicit;
	private final boolean optional;

	public CastedEval(ExpressionCompiler compiler, CodePosition position, TypeID type, boolean explicit, boolean optional) {
		this.compiler = compiler;
		this.position = position;
		this.type = type;
		this.explicit = explicit;
		this.optional = optional;
	}

	public CastedExpression of(Expression value) {
		if (value.type.equals(type) || type == BasicTypeID.UNDETERMINED)
			return new CastedExpression(CastedExpression.Level.EXACT, value);
		if (value.type.isInvalid())
			return CastedExpression.invalidType(value);

		ResolvedType resolvedTargetType = compiler.resolve(type);

		Optional<StaticCallable> implicitConstructor = resolvedTargetType.findImplicitConstructor();
		if (implicitConstructor.isPresent()) {
			CastedExpression fromImplicitConstructor = implicitConstructor.get().casted(compiler, position, this, null, value.wrap(compiler));
			if (!fromImplicitConstructor.isFailed())
				return fromImplicitConstructor;
		}

		ResolvedType resolvedValueType = compiler.resolve(value.type);

		Optional<Expression> implicitCast = resolvedValueType.tryCastImplicit(type, compiler, position, value, optional);
		if (implicitCast.isPresent())
			return new CastedExpression(CastedExpression.Level.IMPLICIT, implicitCast.get());

		if (value.type.canCastImplicitTo(type))
			return new CastedExpression(CastedExpression.Level.IMPLICIT, value.type.castImplicitTo(position, value, type));
		if (type.canCastImplicitFrom(value.type))
			return CastedExpression.implicit(type.castImplicitFrom(position, value));

		if (value.type == BasicTypeID.NULL && type.isOptional())
			return CastedExpression.exact(new NullExpression(position, type));

		// TODO: optional cast (implicit value casting map)
		if (type.isOptional() && !value.type.isOptional()) {
			CastedEval cast = new CastedEval(compiler, position, type.withoutOptional(), explicit, optional);
			CastedExpression result = cast.of(value);
			if (!result.isFailed())
				return new CastedExpression(result.level, new WrapOptionalExpression(position, result.value, type));
		} else if (value.type.isOptional() && !type.isOptional()) {
			CastedExpression result = of(new CheckNullExpression(position, value));
			if (!result.isFailed())
				return result;
		}

		//if (extendsOrImplements(type))
		//	return CastedExpression.implicit(new SupertypeCastExpression(position, value, type));

		if (explicit) {
			Optional<Expression> casted = compiler.resolve(value.type).tryCastExplicit(type, compiler, position, value, optional);
			if (casted.isPresent())
				return new CastedExpression(CastedExpression.Level.EXPLICIT, casted.get());
		}

		return CastedExpression.invalid(position, CompileErrors.cannotCast(value.type, type, explicit));
	}

	public CastedExpression of(CastedExpression.Level level, Expression value) {
		if (level == CastedExpression.Level.EXPLICIT && !explicit)
			return CastedExpression.invalid(position, CompileErrors.cannotCast(value.type, type, explicit));

		CastedExpression casted = of(value);
		return new CastedExpression(level.max(casted.level), casted.value);
	}

	public CastedExpression invalid(CompileError error) {
		return CastedExpression.invalid(position, error);
	}
}
