package org.openzen.zenscript.javashared.types;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.expressions.JavaFunctionInterfaceCastExpression;

import java.lang.reflect.Method;

public class JavaFunctionalInterfaceTypeID extends FunctionTypeID {
	public final Method functionalInterfaceMethod;
	public final JavaMethod method;
	private final FunctionTypeID normalized;

	public JavaFunctionalInterfaceTypeID(GlobalTypeRegistry registry, FunctionHeader header, Method functionalInterfaceMethod, JavaMethod method) {
		super(registry, header);

		this.functionalInterfaceMethod = functionalInterfaceMethod;
		this.method = method;

		FunctionHeader normalizedHeader = header.normalize(registry);
		normalized = header == normalizedHeader ? this : internalizeHeaderChange(registry,normalizedHeader);
	}

	@Override
	public TypeID instance(GenericMapper mapper) {
		return internalizeHeaderChange(mapper.registry,mapper.map(header));
	}

	@Override
	public boolean canCastImplicitTo(TypeID other) {
		return other instanceof FunctionTypeID && ((FunctionTypeID) other).header.isEquivalentTo(header);
	}

	@Override
	public boolean canCastImplicitFrom(TypeID other) {
		return other instanceof FunctionTypeID && ((FunctionTypeID) other).header.isEquivalentTo(header);
	}

	@Override
	public Expression castImplicitTo(CodePosition position, Expression value, TypeID other) {
		if (other instanceof FunctionTypeID) {
			FunctionTypeID otherType = (FunctionTypeID) other;
			if (header.isEquivalentTo(otherType.header))
				return new JavaFunctionInterfaceCastExpression(position, otherType, value);
		}
		return null;
	}

	@Override
	public Expression castImplicitFrom(CodePosition position, Expression value) {
		if (value.type instanceof FunctionTypeID) {
			FunctionTypeID otherType = (FunctionTypeID) value.type;
			if (header.isEquivalentTo(otherType.header))
				return new JavaFunctionInterfaceCastExpression(position, this, value);
		}
		return null;
	}

	private JavaFunctionalInterfaceTypeID internalizeHeaderChange(GlobalTypeRegistry registry, FunctionHeader header) {
		JavaFunctionalInterfaceTypeID normalizedTypeId = new JavaFunctionalInterfaceTypeID(registry, header, functionalInterfaceMethod, method);
		return registry.internalize(JavaFunctionalInterfaceTypeID.class, normalizedTypeId);
	}
}
