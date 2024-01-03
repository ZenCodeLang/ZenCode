package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class LocalTypeImpl implements LocalType {
	private final TypeID thisType;
	private final ResolvedType resolvedThis;
	private ResolvedType resolvedSuper;

	public LocalTypeImpl(TypeID type, TypeResolver resolver) {
		thisType = type;
		resolvedThis = resolver.resolve(thisType);
	}

	@Override
	public TypeID getThisType() {
		return thisType;
	}

	@Override
	public Optional<TypeID> getSuperType() {
		return Optional.ofNullable(thisType.getSuperType());
	}

	@Override
	public StaticCallable thisCall() {
		return resolvedThis.getConstructor().map(constructor -> new ThisCallable(thisType, constructor));
	}

	@Override
	public Optional<StaticCallable> superCall() {
		if (thisType.getSuperType() == null)
			return Optional.empty();

		if (resolvedSuper == null)
			resolvedSuper = thisType.getSuperType().resolve();

		return Optional.ofNullable(resolvedSuper)
				.map(super_ -> super_.getConstructor().map(constructor -> new SuperCallable(thisType.getSuperType(), constructor)));
	}

	private static class ThisCallable implements StaticCallableMethod {
		private final TypeID type;
		private final MethodInstance method;

		public ThisCallable(TypeID type, StaticCallableMethod method) {
			this.type = type;
			this.method = method.asMethod().orElseThrow(() -> new RuntimeException("Constructor which isn't a method!"));
		}

		@Override
		public FunctionHeader getHeader() {
			return method.getHeader();
		}

		@Override
		public Optional<MethodInstance> asMethod() {
			return Optional.of(method);
		}

		@Override
		public Expression call(ExpressionBuilder builder, CallArguments arguments) {
			return builder.constructorThis(type, method, arguments);
		}

		@Override
		public boolean isImplicit() {
			return method.isImplicit();
		}
	}

	private static class SuperCallable implements StaticCallableMethod {
		private final TypeID type;
		private final MethodInstance method;

		public SuperCallable(TypeID type, StaticCallableMethod method) {
			this.type = type;
			this.method = method.asMethod().orElseThrow(() -> new RuntimeException("Constructor which isn't a method!"));
		}

		@Override
		public FunctionHeader getHeader() {
			return method.getHeader();
		}

		@Override
		public Optional<MethodInstance> asMethod() {
			return Optional.of(method);
		}

		@Override
		public Expression call(ExpressionBuilder builder, CallArguments arguments) {
			return builder.constructorSuper(type, method, arguments);
		}

		@Override
		public boolean isImplicit() {
			return method.isImplicit();
		}
	}
}
