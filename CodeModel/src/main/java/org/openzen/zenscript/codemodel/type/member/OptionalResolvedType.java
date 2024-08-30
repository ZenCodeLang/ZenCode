package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.codemodel.type.builtin.OptionalToStringMethod;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OptionalResolvedType implements ResolvedType {
	private final OptionalTypeID type;
	private final ResolvedType baseType;
	private final ResolvedType optionalMembers;

	public OptionalResolvedType(OptionalTypeID type, ResolvedType baseType) {
		this.type = type;
		this.baseType = baseType;

		MemberSet.Builder optionalMembersBuilder = MemberSet.create(type);
		optionalMembersBuilder.method(new MethodInstance(
				BuiltinMethodSymbol.OPTIONAL_IS_NULL,
				new FunctionHeader(BasicTypeID.BOOL, BasicTypeID.NULL),
				type));
		optionalMembersBuilder.method(new MethodInstance(
				BuiltinMethodSymbol.OPTIONAL_IS_NOT_NULL,
				new FunctionHeader(BasicTypeID.BOOL, BasicTypeID.NULL),
				type));
		this.optionalMembers = optionalMembersBuilder.build().withExpansions(Collections.emptyList());
	}

	@Override
	public TypeID getType() {
		return type;
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
		if (toType == BasicTypeID.STRING && baseType.canCastImplicitlyTo(BasicTypeID.STRING)) {
			return Optional.of(new OptionalToStringMethod(type, baseType));
		}
		// TODO: make it castable if the base type can be casted to the target type
		return Optional.empty();
	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		return optionalMembers.findOperator(operator);
	}

	@Override
	public Optional<Field> findField(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {
		return Optional.empty();
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
		return Collections.emptyList();
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		return Optional.empty();
	}

	@Override
	public Optional<StaticCallable> findStaticOperator(OperatorType operator) {
		return Optional.empty();
	}
}
