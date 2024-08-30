package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.member.InterfaceCaster;
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
	private final List<ResolvedType> implementations;
	private final Collection<TypeID> implementedInterfaces;

	public InterfaceResolvedType(ResolvedType baseType, Collection<TypeID> implementedInterfaces) {
		this.baseType = baseType;
		implementations = Stream.concat(Stream.of(baseType), implementedInterfaces.stream().map(TypeID::resolve)).collect(Collectors.toList());
		this.implementedInterfaces = implementedInterfaces;
	}

	@Override
	public TypeID getType() {
		return baseType.getType();
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
			InterfaceCaster interfaceCaster = new InterfaceCaster(new FunctionHeader(toType), new ImplementationMemberInstance(toType));
			return Optional.of(interfaceCaster);
		}

		return findFirstInLocalOrImplementedInterfaces(resolvedType -> resolvedType.findCaster(toType));
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
		return mergeLocalWithImplementedInterfaces(type -> type.findMethod(name), InstanceCallable::merge);
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return mergeLocalWithImplementedInterfaces(type -> type.findGetter(name), InstanceCallable::merge);
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return mergeLocalWithImplementedInterfaces(type -> type.findSetter(name), InstanceCallable::merge);
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		return mergeLocalWithImplementedInterfaces(type -> type.findOperator(operator), InstanceCallable::merge);
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
		return implementations.stream().flatMap(type -> type.comparators().stream())
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

	@Override
	public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
		List<ResolvedType> interfaceExpansions = implementedInterfaces.stream()
				.flatMap(iface -> expansions.stream().map(expansion -> expansion.resolve(iface)).filter(Optional::isPresent).map(Optional::get))
				.collect(Collectors.toList());

		return new InterfaceResolvedType(
				ExpandedResolvedType.of(
					baseType.withExpansions(expansions),
					interfaceExpansions),
				implementedInterfaces);
	}

	private <T> Optional<T> findFirstInLocalOrImplementedInterfaces(Function<ResolvedType, Optional<T>> mapper) {
		return implementations.stream()
				.map(mapper)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	private <T> Optional<T> mergeLocalWithImplementedInterfaces(Function<ResolvedType, Optional<T>> mapper, BinaryOperator<T> combiner) {
		return implementations.stream()
				.map(mapper)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(combiner);
	}
}
