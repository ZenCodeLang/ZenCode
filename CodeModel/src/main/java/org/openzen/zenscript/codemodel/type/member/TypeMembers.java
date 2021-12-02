package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.InnerDefinition;
import org.openzen.zenscript.codemodel.member.ref.*;
import org.openzen.zenscript.codemodel.partial.*;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.*;

import java.util.*;

public final class TypeMembers {
	public final TypeID type;
	private final LocalMemberCache cache;
	private final List<TypeMember<CasterMemberRef>> casters = new ArrayList<>();
	private final List<TypeMember<ImplementationMemberRef>> implementations = new ArrayList<>();
	private final List<TypeMember<IteratorMemberRef>> iterators = new ArrayList<>();

	private final Map<String, EnumConstantMember> enumMembers = new HashMap<>();
	private final Map<String, VariantOptionRef> variantOptions = new HashMap<>();
	private final Map<String, TypeMemberGroup> members = new HashMap<>();
	private final Map<String, InnerDefinition> innerTypes = new HashMap<>();
	private final Map<OperatorType, TypeMemberGroup> operators = new HashMap<>();

	public TypeMembers(LocalMemberCache cache, TypeID type) {
		if (type == null)
			throw new NullPointerException("Type must not be null!");
		if (type == BasicTypeID.UNDETERMINED)
			throw new IllegalArgumentException("Cannot retrieve members of undetermined type");

		this.cache = cache;
		this.type = type;
	}

	public LocalMemberCache getMemberCache() {
		return cache;
	}

	public boolean extendsOrImplements(TypeID other) {
		other = other.getNormalized();
		checkBoundaries:
		if (this.type instanceof DefinitionTypeID && other instanceof DefinitionTypeID) {
			DefinitionTypeID thisTypeId = (DefinitionTypeID) this.type;
			DefinitionTypeID otherTypeId = (DefinitionTypeID) other;

			if (thisTypeId.definition != otherTypeId.definition) {
				break checkBoundaries;
			}

			if (thisTypeId.definition.typeParameters.length != otherTypeId.typeArguments.length) {
				break checkBoundaries;
			}

			for (int i = 0; i < thisTypeId.definition.typeParameters.length; i++) {
				final TypeID type = otherTypeId.typeArguments[i];
				if (type == BasicTypeID.UNDETERMINED) {
					continue;
				}
				if (type instanceof InvalidTypeID && ((InvalidTypeID) type).code == CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE) {
					continue;
				}
				if (thisTypeId.definition.typeParameters[i].matches(cache, type)) {
					continue;
				}
				break checkBoundaries;
			}
			return true;
		}


		TypeID superType = type.getSuperType(cache.getRegistry());
		if (superType != null) {
			if (superType == other)
				return true;
			if (cache.get(superType).extendsOrImplements(other))
				return true;
		}

		for (TypeMember<ImplementationMemberRef> implementation : implementations) {
			if (implementation.member.implementsType == other)
				return true;
			if (cache.get(implementation.member.implementsType).extendsOrImplements(other))
				return true;
		}

		return false;
	}

	public boolean extendsType(TypeID other) {
		other = other.getNormalized();
		TypeID superType = type.getSuperType(cache.getRegistry());
		if (superType != null) {
			return superType == other || cache.get(superType).extendsType(other);
		}

		return false;
	}

	public GlobalTypeRegistry getTypeRegistry() {
		return cache.getRegistry();
	}

	public void copyMembersTo(TypeMembers other, TypeMemberPriority priority) {
		for (TypeMember<CasterMemberRef> caster : casters) {
			other.casters.add(new TypeMember<>(priority, caster.member));
		}
		for (TypeMember<IteratorMemberRef> iterator : iterators) {
			other.addIterator(iterator.member, priority);
		}

		for (Map.Entry<String, EnumConstantMember> entry : enumMembers.entrySet())
			other.addEnumMember(entry.getValue(), priority);
		for (Map.Entry<String, VariantOptionRef> entry : variantOptions.entrySet())
			other.addVariantOption(entry.getValue());
		for (Map.Entry<String, TypeMemberGroup> entry : members.entrySet())
			other.getOrCreateGroup(entry.getKey(), entry.getValue().isStatic).merge(entry.getValue(), priority);
		for (Map.Entry<String, InnerDefinition> entry : innerTypes.entrySet())
			other.innerTypes.put(entry.getKey(), entry.getValue());
		for (Map.Entry<OperatorType, TypeMemberGroup> entry : operators.entrySet())
			other.getOrCreateGroup(entry.getKey()).merge(entry.getValue(), priority);
	}

	public DefinitionMemberRef getBuiltin(BuiltinID builtin) {
		for (TypeMemberGroup group : members.values()) {
			if (group.getConstant() != null && group.getConstant().member.builtin == builtin)
				return group.getConstant();
			if (group.getField() != null && group.getField().member.builtin == builtin)
				return group.getField();

			for (TypeMember<FunctionalMemberRef> member : group.getMethodMembers()) {
				if (member.member.getBuiltin() == builtin)
					return member.member;
			}
		}

		for (TypeMemberGroup group : operators.values()) {
			if (group.getConstant() != null && group.getConstant().member.builtin == builtin)
				return group.getConstant();
			if (group.getField() != null && group.getField().member.builtin == builtin)
				return group.getField();

			for (TypeMember<FunctionalMemberRef> member : group.getMethodMembers()) {
				if (member.member.getBuiltin() == builtin)
					return member.member;
			}
		}

		return null;
	}

	public TypeID union(TypeID other) {
		other = other.getNormalized();
		if (type == other)
			return type;

		if (this.canCastImplicit(other))
			return other;
		if (cache.get(other).canCastImplicit(type))
			return type;


		for (TypeMember<ImplementationMemberRef> implementation : this.implementations) {
			final TypeID union = cache.get(implementation.member.implementsType).union(other);
			if (union != null)
				return union;
		}

		if (this.type instanceof ArrayTypeID && other instanceof ArrayTypeID) {
			ArrayTypeID thisArray = (ArrayTypeID) this.type;
			ArrayTypeID otherArray = (ArrayTypeID) other;

			if (thisArray.dimension == otherArray.dimension) {
				final TypeID union = cache.get(thisArray.elementType).union(otherArray.elementType);
				if (union != null) {
					return getTypeRegistry().getArray(union, thisArray.dimension);
				}
			}
		}


		return null;
	}

	public List<IDefinitionMember> getUnimplementedMembers(Set<IDefinitionMember> implemented) {
		List<IDefinitionMember> result = new ArrayList<>();
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (caster.member.member.isAbstract() && !implemented.contains(caster.member.member))
				result.add(caster.member.member);
		}
		for (TypeMember<IteratorMemberRef> iterator : iterators) {
			if (iterator.member.target.isAbstract() && !implemented.contains(iterator.member.target))
				result.add(iterator.member.target);
		}
		for (Map.Entry<String, TypeMemberGroup> entry : members.entrySet()) {
			TypeMemberGroup group = entry.getValue();
			if (group.getGetter() != null && group.getGetter().member.isAbstract() && !implemented.contains(group.getGetter().member))
				result.add(group.getGetter().member);
			if (group.getSetter() != null && group.getSetter().member.isAbstract() && !implemented.contains(group.getSetter().member))
				result.add(group.getSetter().member);
			for (TypeMember<FunctionalMemberRef> member : group.getMethodMembers())
				if (member.member.getTarget().isAbstract() && !implemented.contains(member.member.getTarget()))
					result.add(member.member.getTarget());
		}
		for (Map.Entry<OperatorType, TypeMemberGroup> entry : operators.entrySet()) {
			if (entry.getKey() == OperatorType.DESTRUCTOR)
				continue; // destructor doesn't have to be implemented; the compiler can do so automatically

			TypeMemberGroup group = entry.getValue();
			if (group.getGetter() != null && group.getGetter().member.isAbstract() && !implemented.contains(group.getGetter().member))
				result.add(group.getGetter().member);
			if (group.getSetter() != null && group.getSetter().member.isAbstract() && !implemented.contains(group.getSetter().member))
				result.add(group.getSetter().member);
			for (TypeMember<FunctionalMemberRef> member : group.getMethodMembers())
				if (member.member.getTarget().isAbstract() && !implemented.contains(member.member.getTarget()))
					result.add(member.member.getTarget());
		}
		return result;
	}

	public void addConstructor(FunctionalMemberRef constructor, TypeMemberPriority priority) {
		getOrCreateGroup(OperatorType.CONSTRUCTOR).addMethod(constructor, priority);
	}

	public void addConstructor(FunctionalMemberRef constructor) {
		getOrCreateGroup(OperatorType.CONSTRUCTOR).addMethod(constructor, TypeMemberPriority.SPECIFIED);
	}

	public void addDestructor(FunctionalMemberRef destructor, TypeMemberPriority priority) {
		getOrCreateGroup(OperatorType.DESTRUCTOR).addMethod(destructor, priority);
	}

	public void addCaller(FunctionalMemberRef caller, TypeMemberPriority priority) {
		getOrCreateGroup(OperatorType.CALL).addMethod(caller, priority);
	}

	public void addCaster(CasterMemberRef caster, TypeMemberPriority priority) {
		for (int i = 0; i < casters.size(); i++) {
			if (casters.get(i).member.toType == caster.toType)
				casters.set(i, casters.get(i).resolve(new TypeMember<>(priority, caster)));
		}

		casters.add(new TypeMember<>(priority, caster));
	}

	public void addConst(ConstMemberRef member) {
		TypeMemberGroup group = getOrCreateGroup(member.member.name, true);
		group.setConst(member, TypeMemberPriority.SPECIFIED);
	}

	public void addField(FieldMemberRef member, TypeMemberPriority priority) {
		TypeMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setField(member, priority);
	}

	public void addGetter(GetterMemberRef member, TypeMemberPriority priority) {
		TypeMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setGetter(member, priority);
	}

	public void addSetter(SetterMemberRef member, TypeMemberPriority priority) {
		TypeMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setSetter(member, priority);
	}

	public void addMethod(String name, FunctionalMemberRef member, TypeMemberPriority priority) {
		TypeMemberGroup group = getOrCreateGroup(name, member.isStatic());
		group.addMethod(member, priority);
	}

	public void addOperator(OperatorType operator, FunctionalMemberRef member) {
		addOperator(operator, member, TypeMemberPriority.SPECIFIED);
	}

	public boolean hasOperator(OperatorType operator) {
		return operators.containsKey(operator) && operators.get(operator).hasMethods();
	}

	public void addOperator(OperatorType operator, FunctionalMemberRef member, TypeMemberPriority priority) {
		TypeMemberGroup group = getOrCreateGroup(operator);
		group.addMethod(member, priority);
	}

	public void addVariantOption(VariantOptionRef option) {
		variantOptions.put(option.getName(), option);
	}

	public void addIterator(IteratorMemberRef iterator, TypeMemberPriority priority) {
		for (int i = 0; i < iterators.size(); i++) {
			if (iterators.get(i).member.getLoopVariableCount() == iterator.getLoopVariableCount()) {
				iterators.set(i, iterators.get(i).resolve(new TypeMember<>(priority, iterator)));
				return;
			}
		}

		iterators.add(new TypeMember<>(priority, iterator));
	}

	public void addImplementation(ImplementationMemberRef member, TypeMemberPriority priority) {
		for (int i = 0; i < implementations.size(); i++) {
			if (implementations.get(i).member.implementsType == member.implementsType) {
				implementations.set(i, implementations.get(i).resolve(new TypeMember<>(priority, member)));
				return;
			}
		}

		implementations.add(new TypeMember<>(priority, member));
	}

	public void addInnerType(String name, InnerDefinition type) {
		innerTypes.put(name, type);
	}

	public TypeMemberGroup getOrCreateGroup(String name, boolean isStatic) {
		if (!members.containsKey(name))
			members.put(name, new TypeMemberGroup(isStatic, name));

		return members.get(name);
	}

	public TypeMemberGroup getGroup(String name) {
		if (!members.containsKey(name))
			return new TypeMemberGroup(false, name);

		return members.get(name);
	}

	public TypeMemberGroup getOrCreateGroup(OperatorType operator) {
		if (!operators.containsKey(operator))
			operators.put(operator, new TypeMemberGroup(false, operator.operator + " operator"));

		return operators.get(operator);
	}

	public TypeMemberGroup getGroup(OperatorType operator) {
		if (!operators.containsKey(operator))
			return new TypeMemberGroup(false, operator.operator + " operator");

		return operators.get(operator);
	}

	public void addEnumMember(EnumConstantMember member, TypeMemberPriority priority) {
		enumMembers.put(member.name, member);
	}

	public EnumConstantMember getEnumMember(String name) {
		return enumMembers.get(name);
	}

	public VariantOptionRef getVariantOption(String name) {
		return variantOptions.get(name);
	}

	public Expression compare(CodePosition position, TypeScope scope, CompareType operator, Expression left, Expression right) throws CompileException {
		if (operator == CompareType.EQ) {
			TypeMemberGroup equal = getOrCreateGroup(OperatorType.EQUALS);
			for (TypeMember<FunctionalMemberRef> member : equal.getMethodMembers()) {
				if (member.member.getHeader().accepts(scope, right))
					return equal.call(position, scope, left, new CallArguments(right), false);
			}
		} else if (operator == CompareType.NE) {
			TypeMemberGroup equal = getOrCreateGroup(OperatorType.NOTEQUALS);
			for (TypeMember<FunctionalMemberRef> member : equal.getMethodMembers()) {
				if (member.member.getHeader().accepts(scope, right)) {
					return equal.call(position, scope, left, new CallArguments(right), false);
				}
			}
			final Expression compare = compare(position, scope, CompareType.EQ, left, right);
			return scope.getTypeMembers(compare.type).unary(position, scope, OperatorType.NOT, compare);
		}

		TypeMemberGroup compare = getOrCreateGroup(OperatorType.COMPARE);
		return compare.callWithComparator(position, scope, left, new CallArguments(right), operator);
	}

	public Expression unary(CodePosition position, TypeScope scope, OperatorType operator, Expression value) throws CompileException {
		TypeMemberGroup members = getOrCreateGroup(operator);
		return members.call(position, scope, value, new CallArguments(Expression.NONE), false);
	}

	public IteratorMemberRef getIterator(int variables) {
		for (TypeMember<IteratorMemberRef> iterator : iterators)
			if (iterator.member.getLoopVariableCount() == variables)
				return iterator.member;

		return null;
	}

	public TypeID[] getLoopTypes(int variables) {
		for (TypeMember<IteratorMemberRef> iterator : iterators)
			if (iterator.member.getLoopVariableCount() == variables)
				return iterator.member.types;

		return null;
	}

	public boolean canCastImplicit(TypeID toType) {
		toType = toType.getNormalized();
		if (type == toType)
			return true;
		if (toType == BasicTypeID.UNDETERMINED)
			throw new IllegalArgumentException("Cannot cast to undetermined type!");
		if (type == BasicTypeID.NULL && toType.isOptional())
			return true;
		if (type.canCastImplicitTo(toType))
			return true;
		if (toType.canCastImplicitFrom(type))
			return true;

		if (toType.isOptional() && canCastImplicit(toType.withoutOptional()))
			return true;
		if (type.isOptional() && type.withoutOptional() == toType)
			return true;

		if (getImplicitCaster(toType) != null || extendsOrImplements(toType))
			return true;

		if (type.isGeneric() && type instanceof GenericTypeID) {
			final GenericTypeID genericTypeID = (GenericTypeID) type;
			return genericTypeID.parameter.matches(cache, toType);
		}

		return false;
	}

	public CasterMemberRef getImplicitCaster(TypeID toType) {
		return getCaster(toType, false);
	}

	public CasterMemberRef getCaster(TypeID toType) {
		return getCaster(toType, true);
	}

	private CasterMemberRef getCaster(TypeID toType, boolean whenExplicit) {
		toType = toType.getNormalized();
		CasterMemberRef foundCaster = null;
		TypeMemberPriority priority = null;
		for (TypeMember<CasterMemberRef> caster : casters) {
			if ((whenExplicit || caster.member.isImplicit()) && caster.member.toType == toType) {
				if (foundCaster == null || priority.compareTo(caster.priority) < 0) {
					foundCaster = caster.member;
					priority = caster.priority;
				}
			}
		}
		return foundCaster;
	}

	public boolean canCast(TypeID toType) {
		toType = toType.getNormalized();
		if (canCastImplicit(toType))
			return true;
		if (type.canCastExplicitTo(toType))
			return true;
		if (toType.canCastImplicitFrom(type))
			return true;

		for (TypeMember<CasterMemberRef> caster : casters) {
			if (caster.member.toType == toType)
				return true;
		}

		return false;
	}

	public Map<DefinitionMemberRef, IDefinitionMember> borrowInterfaceMembersFromDefinition(Set<IDefinitionMember> implemented, TypeMembers definitionMembers) {
		Map<DefinitionMemberRef, IDefinitionMember> result = new HashMap<>();

		for (TypeMember<CasterMemberRef> caster : casters) {
			if (implemented.contains(caster.member.member))
				continue;

			CasterMemberRef implementation = definitionMembers.getCaster(caster.member.toType);
			if (implementation != null)
				result.put(caster.member, implementation.getTarget());
		}
		for (TypeMember<IteratorMemberRef> iterator : iterators) {
			if (implemented.contains(iterator.member.target))
				continue;

			IteratorMemberRef implementation = definitionMembers.getIterator(iterator.member.getLoopVariableCount());
			if (implementation != null)
				result.put(iterator.member, implementation.getTarget());
		}
		for (Map.Entry<String, TypeMemberGroup> entry : members.entrySet()) {
			TypeMemberGroup group = entry.getValue();
			TypeMemberGroup definitionGroup = definitionMembers.getGroup(entry.getKey());
			if (definitionGroup == null)
				continue;

			if (group.getGetter() != null) {
				if (!implemented.contains(group.getGetter().member)) {
					GetterMemberRef implementation = definitionGroup.getGetter();
					if (implementation != null)
						result.put(group.getGetter(), implementation.getTarget());
				}
			}
			if (group.getSetter() != null) {
				if (!implemented.contains(group.getSetter().member)) {
					SetterMemberRef implementation = definitionGroup.getSetter();
					if (implementation != null)
						result.put(group.getSetter(), implementation.getTarget());
				}
			}
			for (TypeMember<FunctionalMemberRef> member : group.getMethodMembers()) {
				if (!implemented.contains(member.member.getTarget())) {
					FunctionalMemberRef functional = definitionGroup.getMethod(member.member.getHeader());
					if (functional != null)
						result.put(member.member, functional.getTarget());
				}
			}
		}
		for (Map.Entry<OperatorType, TypeMemberGroup> entry : operators.entrySet()) {
			if (entry.getKey() == OperatorType.DESTRUCTOR)
				continue; // destructor doesn't have to be implemented; the compiler can do so automatically

			TypeMemberGroup group = entry.getValue();
			TypeMemberGroup definitionGroup = definitionMembers.getGroup(entry.getKey());
			if (definitionGroup == null)
				continue;

			for (TypeMember<FunctionalMemberRef> member : group.getMethodMembers()) {
				if (!implemented.contains(member.member.getTarget())) {
					FunctionalMemberRef functional = definitionGroup.getMethod(member.member.getHeader());
					if (functional != null)
						result.put(member.member, functional.getTarget());
				}
			}
		}

		return result;
	}

	public Expression castImplicit(CodePosition position, Expression value, TypeID toType, boolean implicit) {
		if (toType == null)
			throw new NullPointerException();

		toType = toType.getNormalized();
		if (toType == BasicTypeID.UNDETERMINED)
			return value;
		if (type == toType)
			return value;
		if (type.canCastImplicitTo(toType))
			return type.castImplicitTo(position, value, toType);
		if (toType.canCastImplicitFrom(type))
			return toType.castImplicitFrom(position, value);

		if (type == BasicTypeID.NULL && toType.isOptional())
			return new NullExpression(position, toType);
		if (toType.isOptional() && canCastImplicit(toType.withoutOptional()))
			return new WrapOptionalExpression(position, castImplicit(position, value, toType.withoutOptional(), implicit), toType);
		if (type.isOptional() && type.withoutOptional() == toType)
			return new CheckNullExpression(position, value);

		for (TypeMember<CasterMemberRef> caster : casters) {
			if (caster.member.isImplicit() && caster.member.toType == toType)
				return caster.member.cast(position, value, implicit);
		}
		for (TypeMember<ImplementationMemberRef> implementation : implementations) {
			if (implementation.member.implementsType.getNormalized() == toType)
				return new InterfaceCastExpression(position, value, implementation.member);
		}
		if (extendsOrImplements(toType))
			return new SupertypeCastExpression(position, value, toType);

		return new InvalidExpression(position, toType, CompileExceptionCode.INVALID_CAST, "Could not cast " + this + " to " + toType);
	}

	public Expression castExplicit(CodePosition position, Expression value, TypeID toType, boolean optional) {
		toType = toType.getNormalized();
		if (this.canCastImplicit(toType))
			return castImplicit(position, value, toType, false);
		if (type.canCastExplicitTo(toType))
			return type.castExplicitTo(position, value, toType);

		final TypeMembers typeMembers = cache.get(type);
		if (this.type != typeMembers.type && typeMembers.canCast(toType)) {
			return typeMembers.castExplicit(position, value, toType, optional);
		}

		for (TypeMember<CasterMemberRef> caster : casters)
			if (caster.member.toType == toType)
				return caster.member.cast(position, value, false);


		if (toType.canCastImplicitFrom(type))
			return toType.castImplicitFrom(position, value);




		return new InvalidExpression(position, toType, CompileExceptionCode.INVALID_CAST, "Cannot cast " + this + " to " + toType + ", even explicitly");
	}

	public boolean hasMember(String name) {
		return members.containsKey(name);
	}

	public IPartialExpression getMemberExpression(CodePosition position, TypeScope scope, Expression target, GenericName name, boolean allowStatic) {
		if (members.containsKey(name.name)) {
			TypeMemberGroup group = members.get(name.name);

			if (group.isStatic)
				return new PartialStaticMemberGroupExpression(position, scope, type, group, name.arguments);
			else
				return new PartialMemberGroupExpression(position, scope, target, group, name.arguments, allowStatic);
		}

		if (this.type.isOptional()) {
			return scope.getTypeMembers(this.type.withoutOptional()).getMemberExpression(position, scope, target, name, allowStatic);
		}

		return null;
	}

	public IPartialExpression getStaticMemberExpression(CodePosition position, TypeScope scope, GenericName name) {
		if (members.containsKey(name.name))
			return new PartialStaticMemberGroupExpression(position, scope, type, members.get(name.name), name.arguments);
		if (innerTypes.containsKey(name.name))
			return new PartialTypeExpression(position, innerTypes.get(name.name).instance(cache.getRegistry(), name.arguments, (DefinitionTypeID) type), name.arguments);
		if (variantOptions.containsKey(name.name))
			return new PartialVariantOptionExpression(position, scope, variantOptions.get(name.name));
		if (enumMembers.containsKey(name.name)) {
			return new EnumConstantExpression(position, type, enumMembers.get(name.name));
		}

		if (this.type.isOptional()) {
			return scope.getTypeMembers(this.type.withoutOptional()).getStaticMemberExpression(position, scope, name);
		}

		return null;
	}

	public boolean hasInnerType(String name) {
		return innerTypes.containsKey(name);
	}

	public DefinitionTypeID getInnerType(CodePosition position, GenericName name) {
		if (!innerTypes.containsKey(name.name))
			throw new RuntimeException("No such inner type in " + type + ": " + name.name);

		return innerTypes.get(name.name).instance(cache.getRegistry(), name.arguments, (DefinitionTypeID) type);
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
