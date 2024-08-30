package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;
import java.util.stream.Collectors;

public class MemberSet implements ResolvedType, ResolvingType {
	public static Builder create(TypeID type) {
		MemberSet members = new MemberSet(type);
		return new Builder(members);
	}

	private final TypeID type;
	private final List<StaticCallableMethod> constructors = new ArrayList<>();
	private final Map<MethodID, List<InstanceCallableMethod>> instanceMethods = new HashMap<>();
	private final Map<MethodID, List<StaticCallableMethod>> staticMethods = new HashMap<>();
	private final Map<String, FieldInstance> fields = new HashMap<>();
	private final Map<String, CompilableExpression> contextMembers = new HashMap<>();
	private final Map<String, SwitchMember> switchMembers = new HashMap<>();
	private final Map<String, TypeSymbol> innerTypes = new HashMap<>();
	private final List<IteratorInstance> iterators = new ArrayList<>();
	private final List<Comparator> comparators = new ArrayList<>();

	public MemberSet(TypeID type) {
		this.type = type;
	}

	@Override
	public TypeID getType() {
		return type;
	}

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
		MethodID id = MethodID.staticMethod(suffix);
		if (!staticMethods.containsKey(id))
			return Optional.empty();

		List<StaticCallableMethod> methods = this.staticMethods.get(id).stream()
				.filter(StaticCallableMethod::isImplicit)
				.collect(Collectors.toList());

		return methods.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(methods));
	}

	@Override
	public Optional<InstanceCallableMethod> findCaster(TypeID toType) {
		MethodID id = MethodID.caster(toType);
		if (!instanceMethods.containsKey(id)) {
			return Optional.empty();
		}
		return instanceMethods.get(id).stream().findFirst();
	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		return findStatic(MethodID.staticMethod(name));
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		return findStatic(MethodID.staticGetter(name));
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		return findStatic(MethodID.staticSetter(name));
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		return find(MethodID.instanceMethod(name));
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return find(MethodID.getter(name));
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return find(MethodID.setter(name));
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		return find(MethodID.operator(operator));
	}

	@Override
	public Optional<StaticCallable> findStaticOperator(OperatorType operator) {
		return findStatic(MethodID.staticOperator(operator));
	}

	@Override
	public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
		List<ResolvedType> resolutions = new ArrayList<>();
		for (ExpansionSymbol expansion : expansions) {
			expansion.resolve(getType()).ifPresent(resolutions::add);
		}
		return ExpandedResolvedType.of(this, resolutions);
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
	public List<Comparator> comparators() {
		return comparators;
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		for (IteratorInstance iterator : iterators) {
			if (iterator.getLoopVariableCount() == variables)
				return Optional.of(iterator);
		}

		return Optional.empty();
	}

	private Optional<StaticCallable> findStatic(MethodID id) {
		List<StaticCallableMethod> methods = this.staticMethods.get(id);
		return methods == null || methods.isEmpty() ? Optional.empty() : Optional.of(new StaticCallable(methods));
	}

	private Optional<InstanceCallable> find(MethodID id) {
		List<InstanceCallableMethod> methods = this.instanceMethods.get(id);
		return methods == null || methods.isEmpty() ? Optional.empty() : Optional.of(new InstanceCallable(methods));
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

		public Builder method(MethodInstance method) {
			return method(method.getID(), method);
		}

		public Builder method(MethodID id, MethodInstance method) {
			if (id.isStatic()) {
				target.staticMethods.computeIfAbsent(id, _id -> new ArrayList<>()).add(method);
			} else {
				target.instanceMethods.computeIfAbsent(id, _id -> new ArrayList<>()).add(method);
			}
			return this;
		}

		public Builder field(FieldInstance field) {
			target.fields.put(field.getName(), field);
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

		public Builder method(MethodID id, InstanceCallableMethod method) {
			target.instanceMethods.computeIfAbsent(id, k -> new ArrayList<>()).add(method);
			return this;
		}

		public Builder method(MethodID id, StaticCallableMethod method) {
			target.staticMethods.computeIfAbsent(id, k -> new ArrayList<>()).add(method);
			return this;
		}

		public Builder inner(TypeSymbol type) {
			target.innerTypes.put(type.getName(), type);
			return this;
		}

		public Builder comparator(Comparator comparator) {
			target.comparators.add(comparator);
			return this;
		}

		public Builder iterator(IteratorInstance iterator) {
			target.iterators.add(iterator);
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
