package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.ExpandedResolvedType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	public TypeID getType() {
		return type;
	}

	@Override
	public StaticCallable getConstructor() {
		return new StaticCallable(template.getConstructors().stream()
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList()));
	}

	@Override
	public Optional<StaticCallable> findImplicitConstructor() {
		List<StaticCallableMethod> constructors = Stream.concat(
						template.getConstructors().stream().filter(c -> c.getModifiers().isImplicit()),
						template.getAllMethods().filter(c -> c.getModifiers().isStatic() && c.getModifiers().isImplicit() && c.getHeader().getReturnType().equals(getType()))
				)
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList());

		return constructors.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(constructors));
	}

	@Override
	public Optional<StaticCallable> findSuffixConstructor(String suffix) {
		List<StaticCallableMethod> constructors = template.getMethod(MethodID.staticMethod(suffix)).stream()
				.filter(c -> c.getModifiers().isStatic() && c.getModifiers().isImplicit())
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList());
		return constructors.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(constructors));
	}

	@Override
	public Optional<InstanceCallableMethod> findCaster(TypeID toType) {
		return template.getMethod(MethodID.caster(toType)).stream().findFirst().map(c -> mapper.map(type, c));

	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		return loadStatic(MethodID.staticMethod(name));
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		return loadStatic(MethodID.getter(name));
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		return loadStatic(MethodID.setter(name));
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		return load(MethodID.instanceMethod(name));
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return load(MethodID.getter(name));
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return load(MethodID.setter(name));
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		return load(MethodID.operator(operator));
	}

	@Override
	public Optional<StaticCallable> findStaticOperator(OperatorType operator) {
		return loadStatic(MethodID.staticOperator(operator));
	}

	@Override
	public Optional<Field> findField(String name) {
		return template.getField(name).map(f -> new RuntimeField(mapper.map(f)));
	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {
		return template.getInnerType(name).map(UnaryOperator.identity());
	}

	@Override
	public Optional<CompilableExpression> getContextMember(String name) {
		return template.getContextMember(name);
	}

	@Override
	public Optional<SwitchMember> findSwitchMember(String name) {
		return Optional.empty(); // TODO
	}

	@Override
	public List<Comparator> comparators() {
		return Collections.emptyList();
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		return Optional.empty();
	}

	private Optional<StaticCallable> loadStatic(MethodID id) {
		List<StaticCallableMethod> methods = template.getMethod(id).stream()
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList());
		return methods.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(methods));
	}

	private Optional<InstanceCallable> load(MethodID id) {
		List<InstanceCallableMethod> methods = template.getMethod(id).stream()
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList());
		return methods.isEmpty() ? Optional.empty() : Optional.of(new InstanceCallable(methods));
	}

	private static class RuntimeField implements Field {
		private final FieldInstance field;

		public RuntimeField(FieldInstance field) {
			this.field = field;
		}

		@Override
		public TypeID getType() {
			return field.getType();
		}

		@Override
		public boolean isStatic() {
			return field.getModifiers().isStatic();
		}

		@Override
		public Expression get(ExpressionBuilder builder, Expression target) {
			return builder.getInstanceField(target, field);
		}

		@Override
		public Expression set(ExpressionBuilder builder, Expression target, Expression value) {
			return builder.setInstanceField(target, field, value);
		}

		@Override
		public Expression getStatic(ExpressionBuilder builder) {
			return builder.getStaticField(field);
		}

		@Override
		public Expression setStatic(ExpressionBuilder builder, Expression value) {
			return builder.setStaticField(field, value);
		}
	}

	public static class Resolving implements ResolvingType {
		private final JavaNativeTypeTemplate template;
		private final TypeID type;
		private final GenericMapper mapper;

		public Resolving(JavaNativeTypeTemplate template, TypeID type, GenericMapper mapper) {
			this.template = template;
			this.type = type;
			this.mapper = mapper;
		}

		@Override
		public TypeID getType() {
			return type;
		}

		@Override
		public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
			return ExpandedResolvedType.resolve(new JavaNativeTypeMembers(template, type, mapper), expansions);
		}
	}
}
