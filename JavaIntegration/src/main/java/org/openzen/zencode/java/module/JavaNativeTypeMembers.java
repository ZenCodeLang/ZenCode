package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaNativeTypeMembers implements ResolvedType {
	private final JavaNativeTypeTemplate template;
	private final TypeID type;
	private final GenericMapper mapper;

	public JavaNativeTypeMembers(JavaNativeTypeTemplate template, TypeID type, GenericMapper mapper) {
		this.template = template;
		this.type = type;
		this.mapper = mapper;
	}

	@Override
	public StaticCallable getConstructor() {
		return new StaticCallable(template.getConstructors().stream()
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList()));
	}

	@Override
	public Optional<StaticCallable> findImplicitConstructor() {
		List<StaticCallableMethod> constructors = template.getConstructors().stream()
				.filter(c -> c.getModifiers().isImplicit())
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList());
		return constructors.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(constructors));
	}

	@Override
	public Optional<StaticCallable> findSuffixConstructor(String suffix) {
		List<StaticCallableMethod> constructors = template.getMethod(suffix).stream()
				.filter(c -> c.getModifiers().isStatic() && c.getModifiers().isImplicit())
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList());
		return constructors.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(constructors));
	}

	@Override
	public Optional<InstanceCallableMethod> findCaster(TypeID toType) {

	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {

	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {

	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {

	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {

	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {

	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {

	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {

	}

	@Override
	public Optional<Field> findField(String name) {

	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {

	}

	@Override
	public Optional<CompilableExpression> getContextMember(String name) {

	}

	@Override
	public Optional<SwitchMember> findSwitchMember(String name) {

	}

	@Override
	public Optional<Comparator> compare() {
		return Optional.empty();
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		return Optional.empty();
	}
}
