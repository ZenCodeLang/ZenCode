package org.openzen.zenscript.javashared.types;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaNativeMethod;
import org.openzen.zenscript.javashared.expressions.JavaFunctionInterfaceCastExpression;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class JavaFunctionalInterfaceTypeID extends FunctionTypeID {
	public final Method functionalInterfaceMethod;
	public final JavaNativeMethod method;

	public JavaFunctionalInterfaceTypeID(FunctionHeader header, Method functionalInterfaceMethod, JavaNativeMethod method) {
		super(header);

		this.functionalInterfaceMethod = functionalInterfaceMethod;
		this.method = method;
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return new JavaFunctionalInterfaceTypeID(mapper.map(header), functionalInterfaceMethod, method);
	}

	@Override
	public Optional<Expression> castImplicitTo(
			CodePosition position,
			Expression value,
			TypeID other,
			List<ExpansionSymbol> expansions) {
		if (other instanceof FunctionTypeID) {
			FunctionTypeID otherType = (FunctionTypeID) other;
			if (header.isEquivalentTo(otherType.header))
				return Optional.of(new JavaFunctionInterfaceCastExpression(position, otherType, value));
		}

		return Optional.empty();
	}

	@Override
	public Optional<Expression> castImplicitFrom(CodePosition position, Expression value, List<ExpansionSymbol> expansions) {
		if (value.type instanceof FunctionTypeID) {
			FunctionTypeID otherType = (FunctionTypeID) value.type;
			if (header.isEquivalentTo(otherType.header))
				return Optional.of(new JavaFunctionInterfaceCastExpression(position, this, value));
		}
		return Optional.empty();
	}
}
