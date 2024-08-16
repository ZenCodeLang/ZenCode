package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.SupertypeCastExpression;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SubclassResolvedType implements ResolvedType {
	private final ResolvedType superclass;
	private final ResolvedType resolved;
	private final TypeID supertype;

	public SubclassResolvedType(ResolvedType superclass, ResolvedType resolved, TypeID supertype) {
		this.superclass = superclass;
		this.resolved = resolved;
		this.supertype = supertype;
	}

	@Override
	public StaticCallable getConstructor() {
		return resolved.getConstructor();
	}

	@Override
	public Optional<StaticCallable> findImplicitConstructor() {
		return resolved.findImplicitConstructor();
	}

	@Override
	public Optional<StaticCallable> findSuffixConstructor(String suffix) {
		return resolved.findSuffixConstructor(suffix);
	}

	@Override
	public Optional<InstanceCallableMethod> findCaster(TypeID toType) {
		if (toType.equals(supertype)) {
			return Optional.of(new SuperCastCallable(supertype));
		}
		return or(resolved.findCaster(toType), () -> superclass.findCaster(toType));
	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		return mergeStatic(resolved.findStaticMethod(name), superclass.findStaticMethod(name));
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		return mergeStatic(resolved.findStaticGetter(name), superclass.findStaticGetter(name));
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		return mergeStatic(resolved.findStaticSetter(name), superclass.findStaticSetter(name));
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		return merge(resolved.findMethod(name), superclass.findMethod(name));
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return merge(resolved.findGetter(name), superclass.findGetter(name));
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return merge(resolved.findSetter(name), superclass.findSetter(name));
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		return merge(resolved.findOperator(operator), superclass.findOperator(operator));
	}

	@Override
	public Optional<StaticCallable> findStaticOperator(OperatorType operator) {
		return mergeStatic(resolved.findStaticOperator(operator), superclass.findStaticOperator(operator));
	}

	@Override
	public Optional<Field> findField(String name) {
		return or(resolved.findField(name), () -> superclass.findField(name));
	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {
		return or(resolved.findInnerType(name), () -> superclass.findInnerType(name));
	}

	@Override
	public Optional<CompilableExpression> getContextMember(String name) {
		return or(resolved.getContextMember(name), () -> superclass.getContextMember(name));
	}

	@Override
	public Optional<SwitchMember> findSwitchMember(String name) {
		return or(resolved.findSwitchMember(name), () -> superclass.findSwitchMember(name));
	}

	@Override
	public List<Comparator> comparators() {
		List<Comparator> result = new ArrayList<>(resolved.comparators());
		result.addAll(superclass.comparators());
		return result;
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		return or(resolved.findIterator(variables), () -> superclass.findIterator(variables));
	}

	private static <T> Optional<T> or(Optional<T> value, Supplier<Optional<T>> other) {
		if (value.isPresent())
			return value;
		return other.get();
	}

	private static Optional<InstanceCallable> merge(Optional<InstanceCallable> a, Optional<InstanceCallable> b) {
		if (!a.isPresent())
			return b;
        return b.map(instanceCallable -> Optional.of(a.get().merge(instanceCallable))).orElse(a);
    }

	private static Optional<StaticCallable> mergeStatic(Optional<StaticCallable> a, Optional<StaticCallable> b) {
		if (!a.isPresent())
			return b;
		return b.map(instanceCallable -> Optional.of(a.get().merge(instanceCallable))).orElse(a);
	}

	private static class SuperCastCallable implements InstanceCallableMethod {
		private final TypeID supertype;

		public SuperCastCallable(TypeID supertype) {
			this.supertype = supertype;
		}

		@Override
		public FunctionHeader getHeader() {
			return new FunctionHeader(supertype);
		}

		@Override
		public Optional<MethodInstance> asMethod() {
			return Optional.empty();
		}

		@Override
		public AnyMethod withGenericArguments(GenericMapper mapper) {
			return new SuperCastCallable(mapper.map(supertype));
		}

		@Override
		public Modifiers getModifiers() {
			return Modifiers.PUBLIC.withImplicit();
		}

		@Override
		public Expression call(ExpressionBuilder builder, Expression instance, CallArguments arguments) {
			return new SupertypeCastExpression(instance.position, instance, supertype);
		}
	}
}
