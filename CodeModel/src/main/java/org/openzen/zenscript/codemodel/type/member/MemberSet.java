package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;
import java.util.stream.Collectors;

public class MemberSet implements ResolvedType {
	public static Builder create() {
		MemberSet members = new MemberSet();
		return new Builder(members);
	}

	private final List<StaticCallableMethod> constructors = new ArrayList<>();
	private final Map<String, InstanceCallableMethod> getters = new HashMap<>();
	private final Map<String, StaticCallableMethod> staticGetters = new HashMap<>();
	private final Map<String, InstanceCallableMethod> setters = new HashMap<>();
	private final Map<String, StaticCallableMethod> staticSetters = new HashMap<>();
	private final Map<String, FieldInstance> fields = new HashMap<>();
	private final Map<String, List<InstanceCallableMethod>> methods = new HashMap<>();
	private final Map<OperatorType, List<InstanceCallableMethod>> operators = new HashMap<>();
	private final Map<String, List<StaticCallableMethod>> staticMethods = new HashMap<>();
	private final List<InstanceCallableMethod> casters = new ArrayList<>();
	private final List<InstanceCallableMethod> iterators = new ArrayList<>();
	private final Map<String, CompilableExpression> contextMembers = new HashMap<>();
	private final Map<String, SwitchMember> switchMembers = new HashMap<>();
	private final Map<String, TypeSymbol> innerTypes = new HashMap<>();
	private InstanceCallableMethod destructor;
	private Comparator comparator;

	@Override
	public StaticCallable getConstructor() {
		return new StaticCallable(constructors);
	}

	@Override
	public Optional<StaticCallable> findImplicitConstructor() {
		List<StaticCallableMethod> constructors = this.constructors.stream()
				.filter(StaticCallableMethod::isImplicit)
				.collect(Collectors.toList());

		return constructors.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(constructors));
	}

	@Override
	public Optional<StaticCallable> findSuffixConstructor(String suffix) {
		if (!staticMethods.containsKey(suffix))
			return Optional.empty();

		List<StaticCallableMethod> methods = staticMethods.get(suffix).stream()
				.filter(StaticCallableMethod::isImplicit)
				.collect(Collectors.toList());

		return methods.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(methods));
	}

	@Override
	public Optional<InstanceCallableMethod> findCaster(TypeID toType) {
		for (InstanceCallableMethod method : casters) {
			if (method.getHeader().getReturnType().equals(toType))
				return Optional.of(method);
		}

		return Optional.empty();
	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		if (staticMethods.containsKey(name)) {
			return Optional.of(new StaticCallable(staticMethods.get(name)));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		return Optional.ofNullable(staticGetters.get(name)).map(StaticCallable::new);
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		return Optional.ofNullable(staticSetters.get(name)).map(StaticCallable::new);
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		if (methods.containsKey(name))
			return Optional.of(new InstanceCallable(methods.get(name)));
		else
			return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return Optional.ofNullable(getters.get(name)).map(InstanceCallable::new);
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return Optional.ofNullable(setters.get(name)).map(InstanceCallable::new);
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		if (operators.containsKey(operator))
			return Optional.of(new InstanceCallable(operators.get(operator)));
		else
			return Optional.empty();
	}

	@Override
	public Optional<Field> findField(String name) {
		return Optional.ofNullable(fields.get(name));
	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {
		return Optional.ofNullable(innerTypes.get(name));
	}

	@Override
	public Optional<CompilableExpression> getContextMember(String name) {
		return Optional.ofNullable(contextMembers.get(name));
	}

	@Override
	public Optional<SwitchMember> findSwitchMember(String name) {
		return Optional.ofNullable(switchMembers.get(name));
	}

	@Override
	public Optional<Comparator> compare() {
		return Optional.ofNullable(comparator);
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	public static class Builder {
		private final MemberSet target;

		public Builder(MemberSet target) {
			this.target = target;
		}

		public Builder constructor(StaticCallableMethod constructor) {
			target.constructors.add(constructor);
			return this;
		}

		public Builder getter(MethodInstance method) {
			return getter(method.getName(), method);
		}

		public Builder getter(String name, InstanceCallableMethod getter) {
			target.getters.put(name, getter);
			return this;
		}

		public Builder setter(MethodInstance method) {
			return setter(method.getName(), method);
		}

		public Builder setter(String name, InstanceCallableMethod setter) {
			target.setters.put(name, setter);
			return this;
		}

		public Builder staticGetter(MethodInstance method) {
			return staticGetter(method.getName(), method);
		}

		public Builder staticGetter(String name, StaticCallableMethod getter) {
			target.staticGetters.put(name, getter);
			return this;
		}

		public Builder staticSetter(MethodInstance method) {
			return staticSetter(method.getName(), method);
		}

		public Builder staticSetter(String name, StaticCallableMethod setter) {
			target.staticSetters.put(name, setter);
			return this;
		}

		public Builder field(FieldInstance field) {
			target.fields.put(field.getName(), field);
			return this;
		}

		public Builder method(MethodInstance method) {
			return method(method.getName(), method);
		}

		public Builder method(String name, InstanceCallableMethod method) {
			target.methods.computeIfAbsent(name, k -> new ArrayList<>()).add(method);
			return this;
		}

		public Builder operator(MethodInstance method) {
			OperatorType operator = method.method.getOperator()
					.orElseThrow(() -> new IllegalArgumentException("Not an operator"));
			return operator(operator, method);
		}

		public Builder operator(OperatorType operator, InstanceCallableMethod method) {
			target.operators.computeIfAbsent(operator, k -> new ArrayList<>()).add(method);
			return this;
		}

		public Builder contextMember(String name, CompilableExpression value) {
			target.contextMembers.put(name, value);
			return this;
		}

		public Builder switchValue(String name, SwitchMember member) {
			target.switchMembers.put(name, member);
			return this;
		}

		/* Operators */

		public Builder contains(InstanceCallableMethod method) {
			return operator(OperatorType.CONTAINS, method);
		}

		public Builder indexGet(InstanceCallableMethod method) {
			return operator(OperatorType.INDEXGET, method);
		}

		public Builder indexSet(InstanceCallableMethod method) {
			return operator(OperatorType.INDEXSET, method);
		}

		public Builder equals(InstanceCallableMethod method) {
			return operator(OperatorType.EQUALS, method);
		}

		public Builder notEquals(InstanceCallableMethod method) {
			return operator(OperatorType.NOTEQUALS, method);
		}

		public Builder same(InstanceCallableMethod method) {
			return operator(OperatorType.SAME, method);
		}

		public Builder notSame(InstanceCallableMethod method) {
			return operator(OperatorType.NOTSAME, method);
		}

		public Builder iterator(InstanceCallableMethod method) {
			target.iterators.add(method);
			return this;
		}

		/* End of operators */

		public Builder staticMethod(MethodInstance method) {
			return staticMethod(method.getName(), method);
		}

		public Builder staticMethod(String name, StaticCallableMethod method) {
			target.staticMethods.computeIfAbsent(name, k -> new ArrayList<>()).add(method);
			return this;
		}

		public Builder cast(InstanceCallableMethod method) {
			target.casters.add(method);
			return this;
		}

		public Builder destructor(InstanceCallableMethod method) {
			target.destructor = method;
			return this;
		}

		public Builder inner(TypeSymbol type) {
			target.innerTypes.put(type.getName(), type);
			return this;
		}

		public Builder comparator(Comparator comparator) {
			target.comparator = comparator;
			return this;
		}

		/* Methods used during final stage of member assembly */
		public boolean hasNoConstructor() {
			return target.constructors.isEmpty();
		}

		public MemberSet build() {
			return target;
		}
	}
}
