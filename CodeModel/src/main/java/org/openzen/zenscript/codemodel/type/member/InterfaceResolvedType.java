package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterfaceResolvedType implements ResolvedType {
	private final ResolvedType baseType;
	private final Collection<TypeID> implementedInterfaces;

	public InterfaceResolvedType(ResolvedType baseType, Collection<TypeID> implementedInterfaces) {
		this.baseType = baseType;
		this.implementedInterfaces = implementedInterfaces;
	}


	@Override
	public StaticCallable getConstructor() {
		return baseType.getConstructor();
	}

	@Override
	public Optional<StaticCallable> findImplicitConstructor() {
		return baseType.findImplicitConstructor();
	}

	@Override
	public Optional<StaticCallable> findSuffixConstructor(String suffix) {
		return baseType.findSuffixConstructor(suffix);
	}

	@Override
	public Optional<InstanceCallableMethod> findCaster(TypeID toType) {
		if (implementedInterfaces.contains(toType)) {
			ImplementationMember.InterfaceCaster interfaceCaster = new ImplementationMember.InterfaceCaster(new FunctionHeader(toType), new ImplementationMemberInstance(toType));
			return Optional.of(interfaceCaster);
		}

		return findFirstInLocalOrImplementedInterfaces(resolvedType -> findCaster(toType));
	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		return baseType.findStaticMethod(name);
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		return baseType.findStaticGetter(name);
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		return baseType.findStaticSetter(name);
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		return mergeLocalWithImplementedInterfaces(type -> type.findMethod(name), InstanceCallable::union);
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return mergeLocalWithImplementedInterfaces(type -> type.findGetter(name), InstanceCallable::union);
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return mergeLocalWithImplementedInterfaces(type -> type.findSetter(name), InstanceCallable::union);
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		return findFirstInLocalOrImplementedInterfaces(type -> type.findOperator(operator));
	}

	@Override
	public Optional<Field> findField(String name) {
		return findFirstInLocalOrImplementedInterfaces(type -> type.findField(name));
	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {
		return findFirstInLocalOrImplementedInterfaces(type -> type.findInnerType(name));
	}

	@Override
	public Optional<CompilableExpression> getContextMember(String name) {
		return baseType.getContextMember(name);
	}

	@Override
	public Optional<SwitchMember> findSwitchMember(String name) {
		return baseType.findSwitchMember(name);
	}

	@Override
	public List<Comparator> comparators() {
		return Stream.concat(
						Stream.of(baseType),
						implementedInterfaces.stream().map(TypeID::resolve)
				).flatMap(type -> type.comparators().stream())
				.collect(Collectors.toList());
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		return findFirstInLocalOrImplementedInterfaces(type -> type.findIterator(variables));
	}

	@Override
	public Optional<StaticCallable> findStaticOperator(OperatorType operator) {
		return baseType.findStaticOperator(operator);
	}

	private <T> Optional<T> findFirstInLocalOrImplementedInterfaces(Function<ResolvedType, Optional<T>> mapper) {
		return Stream.concat(
						Stream.of(baseType),
						implementedInterfaces.stream().map(TypeID::resolve)
				)
				.map(mapper)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	private <T> Optional<T> mergeLocalWithImplementedInterfaces(Function<ResolvedType, Optional<T>> mapper, BinaryOperator<T> combiner) {
		return Stream.concat(
						Stream.of(baseType),
						implementedInterfaces.stream().map(TypeID::resolve)
				)
				.map(mapper)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(combiner);
	}
}
