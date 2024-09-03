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
import org.openzen.zenscript.codemodel.member.InterfaceCaster;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubtypeResolvedType implements ResolvedType {

	private final ResolvedType thisType;
	private final Collection<ResolvedType> baseTypes;
	private final Kind subtypeKind;

	private SubtypeResolvedType(ResolvedType thisType, Collection<ResolvedType> baseTypes, Kind subtypeKind) {
		this.thisType = thisType;
		this.baseTypes = baseTypes;
		this.subtypeKind = subtypeKind;
	}

	public static ResolvedType ofChildClass(ResolvedType thisType, ResolvedType superTypes) {
		return of(thisType, Collections.singletonList(superTypes), Kind.SUBCLASS);
	}

	public static ResolvedType ofImplementation(ResolvedType thisType, Collection<ResolvedType> superTypes) {
		return of(thisType, superTypes, Kind.IMPLEMENTATION);
	}

	private static ResolvedType of(ResolvedType thisType, Collection<ResolvedType> superTypes, Kind isChildClass) {
		if(superTypes.isEmpty()){
			return thisType;
		}

		return new SubtypeResolvedType(thisType, superTypes, isChildClass);
	}

	@Override
	public TypeID getType() {
		return thisType.getType();
	}

	@Override
	public StaticCallable getConstructor() {
		return thisType.getConstructor();
	}

	@Override
	public Optional<StaticCallable> findImplicitConstructor() {
		return thisType.findImplicitConstructor();
	}

	@Override
	public Optional<StaticCallable> findSuffixConstructor(String suffix) {
		return thisType.findSuffixConstructor(suffix);
	}

	@Override
	public Optional<InstanceCallableMethod> findCaster(TypeID toType) {
		Optional<InstanceCallableMethod> supertypeCaster = baseTypes.stream()
				.filter(superType -> superType.getType().equals(toType))
				.map(superType -> {
					switch (subtypeKind) {
						case SUBCLASS:
							return new SuperCastCallable(superType.getType());
						case IMPLEMENTATION:
							return new InterfaceCaster(new FunctionHeader(toType), new ImplementationMemberInstance(toType));
						default:
							throw new IllegalStateException("");
					}
				})
				.findFirst();
		if(supertypeCaster.isPresent()) {
			return supertypeCaster;
		}

		return findFirstInLocalOrBaseTypes(resolvedType -> resolvedType.findCaster(toType));
	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		return mergeLocalWithBaseTypes(type -> type.findStaticMethod(name), StaticCallable::merge);
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		return mergeLocalWithBaseTypes(type -> type.findStaticGetter(name), StaticCallable::merge);
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		return mergeLocalWithBaseTypes(type -> type.findStaticSetter(name), StaticCallable::merge);
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		return mergeLocalWithBaseTypes(type -> type.findMethod(name), InstanceCallable::merge);
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return mergeLocalWithBaseTypes(type -> type.findGetter(name), InstanceCallable::merge);
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return mergeLocalWithBaseTypes(type -> type.findSetter(name), InstanceCallable::merge);
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		return mergeLocalWithBaseTypes(type -> type.findOperator(operator), InstanceCallable::merge);
	}

	@Override
	public Optional<Field> findField(String name) {
		return findFirstInLocalOrBaseTypes(type -> type.findField(name));
	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {
		return findFirstInLocalOrBaseTypes(type -> type.findInnerType(name));
	}

	@Override
	public Optional<CompilableExpression> getContextMember(String name) {
		return thisType.getContextMember(name);
	}

	@Override
	public Optional<SwitchMember> findSwitchMember(String name) {
		return thisType.findSwitchMember(name);
	}

	@Override
	public List<Comparator> comparators() {
		return streamAllTypes()
				.flatMap(type -> type.comparators().stream())
				.collect(Collectors.toList());
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		return findFirstInLocalOrBaseTypes(type -> type.findIterator(variables));
	}

	@Override
	public Optional<StaticCallable> findStaticOperator(OperatorType operator) {
		return thisType.findStaticOperator(operator);
	}

	private <T> Optional<T> findFirstInLocalOrBaseTypes(Function<ResolvedType, Optional<T>> mapper) {
		return streamAllTypes()
				.map(mapper)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	private <T> Optional<T> mergeLocalWithBaseTypes(Function<ResolvedType, Optional<T>> mapper, BinaryOperator<T> combiner) {
		return streamAllTypes()
				.map(mapper)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(combiner);
	}

	private Stream<ResolvedType> streamAllTypes() {
		return Stream.concat(
				Stream.of(thisType),
				baseTypes.stream()
		);
	}

	private enum Kind {
		SUBCLASS,
		IMPLEMENTATION
	}

	private static final class SuperCastCallable implements InstanceCallableMethod {
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
