/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CheckNullExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InterfaceCastExpression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.expression.StorageCastExpression;
import org.openzen.zenscript.codemodel.expression.SupertypeCastExpression;
import org.openzen.zenscript.codemodel.expression.WrapOptionalExpression;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.InnerDefinition;
import org.openzen.zenscript.codemodel.member.ref.CasterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.ConstMemberRef;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberRef;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.member.ref.SetterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialMemberGroupExpression;
import org.openzen.zenscript.codemodel.partial.PartialStaticMemberGroupExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.partial.PartialVariantOptionExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public final class TypeMembers {
	private final LocalMemberCache cache;
	public final StoredType type;
	
	private final List<TypeMember<CasterMemberRef>> casters = new ArrayList<>();
	private final List<TypeMember<ImplementationMemberRef>> implementations = new ArrayList<>();
	private final List<TypeMember<IteratorMemberRef>> iterators = new ArrayList<>();
	
	private final Map<String, EnumConstantMember> enumMembers = new HashMap<>();
	private final Map<String, VariantOptionRef> variantOptions = new HashMap<>();
	private final Map<String, TypeMemberGroup> members = new HashMap<>();
	private final Map<String, InnerDefinition> innerTypes = new HashMap<>();
	private final Map<OperatorType, TypeMemberGroup> operators = new HashMap<>();
	
	public TypeMembers(LocalMemberCache cache, StoredType type) {
		if (type == null)
			throw new NullPointerException("Type must not be null!");
		if (type.type == BasicTypeID.UNDETERMINED)
			throw new IllegalArgumentException("Cannot retrieve members of undetermined type");
		
		this.cache = cache;
		this.type = type;
	}
	
	public LocalMemberCache getMemberCache() {
		return cache;
	}
	
	public boolean extendsOrImplements(TypeID other) {
		other = other.getNormalized();
		TypeID superType = type.type.getSuperType(cache.getRegistry());
		if (superType != null) {
			if (superType == other)
				return true;
			if (cache.get(superType.stored(type.getActualStorage())).extendsOrImplements(other))
				return true;
		}
		
		for (TypeMember<ImplementationMemberRef> implementation : implementations) {
			if (implementation.member.implementsType.type == other)
				return true;
			if (cache.get(implementation.member.implementsType).extendsOrImplements(other))
				return true;
		}
		
		return false;
	}
	
	public boolean extendsType(TypeID other) {
		other = other.getNormalized();
		TypeID superType = type.type.getSuperType(cache.getRegistry());
		if (superType != null) {
			if (superType == other)
				return true;
			if (cache.get(superType.stored(type.getActualStorage())).extendsType(other))
				return true;
		}
		
		return false;
	}
	
	public GlobalTypeRegistry getTypeRegistry() {
		return cache.getRegistry();
	}
	
	public void copyMembersTo(CodePosition position, TypeMembers other, TypeMemberPriority priority) {
		other.casters.addAll(casters);
		other.iterators.addAll(iterators);
		
		for (Map.Entry<String, EnumConstantMember> entry : enumMembers.entrySet())
			other.addEnumMember(entry.getValue(), priority);
		for (Map.Entry<String, VariantOptionRef> entry : variantOptions.entrySet())
			other.addVariantOption(entry.getValue());
		for (Map.Entry<String, TypeMemberGroup> entry : members.entrySet())
			other.getOrCreateGroup(entry.getKey(), entry.getValue().isStatic).merge(position, entry.getValue(), priority);
		for (Map.Entry<String, InnerDefinition> entry : innerTypes.entrySet())
			other.innerTypes.put(entry.getKey(), entry.getValue());
		for (Map.Entry<OperatorType, TypeMemberGroup> entry : operators.entrySet())
			other.getOrCreateGroup(entry.getKey()).merge(position, entry.getValue(), priority);
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
	
	public StoredType union(StoredType other) {
		other = other.getNormalized();
		if (type == other)
			return type;
		
		if (this.canCastImplicit(other))
			return other;
		if (cache.get(other).canCastImplicit(type))
			return type;
		
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
	
	public void addCaster(CasterMemberRef caster) throws CompileException {
		addCaster(caster, TypeMemberPriority.SPECIFIED);
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
	
	public void addField(FieldMemberRef member) {
		addField(member, TypeMemberPriority.SPECIFIED);
	}
	
	public void addField(FieldMemberRef member, TypeMemberPriority priority) {
		TypeMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setField(member, priority);
	}
	
	public void addGetter(GetterMemberRef member) throws CompileException {
		addGetter(member, TypeMemberPriority.SPECIFIED);
	}
	
	public void addGetter(GetterMemberRef member, TypeMemberPriority priority) {
		TypeMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setGetter(member, priority);
	}
	
	public void addSetter(SetterMemberRef member) throws CompileException {
		addSetter(member, TypeMemberPriority.SPECIFIED);
	}
	
	public void addSetter(SetterMemberRef member, TypeMemberPriority priority) {
		TypeMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setSetter(member, priority);
	}
	
	public void addMethod(String name, FunctionalMemberRef member) {
		addMethod(name, member, TypeMemberPriority.SPECIFIED);
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
		return members.get(name);
	}
	
	public TypeMemberGroup getOrCreateGroup(OperatorType operator) {
		if (!operators.containsKey(operator))
			operators.put(operator, new TypeMemberGroup(false, operator.operator + " operator"));
		
		return operators.get(operator);
	}
	
	public TypeMemberGroup getGroup(OperatorType operator) {
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
	
	public StoredType[] getLoopTypes(int variables) {
		for (TypeMember<IteratorMemberRef> iterator : iterators)
			if (iterator.member.getLoopVariableCount() == variables)
				return iterator.member.types;
		
		return null;
	}
	
	public boolean canCastImplicit(StoredType toType) {
		toType = toType.getNormalized();
		if (areEquivalent(type, toType))
			return true;
		if (toType.type == BasicTypeID.UNDETERMINED)
			throw new IllegalArgumentException("Cannot cast to undetermined type!");
		if (type.type == BasicTypeID.NULL && toType.type.isOptional())
			return true;
		
		if (!type.getActualStorage().canCastTo(toType.getActualStorage()) && !toType.getActualStorage().canCastFrom(type.getActualStorage()))
			return false;
		
		if (toType.isOptional() && canCastImplicit(toType.withoutOptional()))
			return true;
		if (type.isOptional() && areEquivalent(type.withoutOptional(), toType))
			return true;
		
		return getImplicitCaster(toType) != null || extendsOrImplements(toType.type);
	}
	
	private boolean areEquivalent(StoredType fromType, StoredType toType) {
		if (fromType == toType)
			return true;
		if (!fromType.getActualStorage().canCastTo(toType.getActualStorage()) && !toType.getActualStorage().canCastFrom(fromType.getActualStorage()))
			return false;
		
		return fromType.type == toType.type;
	}
	
	public CasterMemberRef getImplicitCaster(StoredType toType) {
		toType = toType.getNormalized();
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (caster.member.isImplicit() && areEquivalent(caster.member.toType, toType))
				return caster.member;
		}
		
		return null;
	}
	
	public CasterMemberRef getCaster(StoredType toType) {
		toType = toType.getNormalized();
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (areEquivalent(caster.member.toType, toType))
				return caster.member;
		}
		
		return null;
	}
	
	public boolean canCast(StoredType toType) {
		toType = toType.getNormalized();
		if (canCastImplicit(toType))
			return true;
		
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (areEquivalent(caster.member.toType, toType))
				return true;
		}
		
		return false;
	}
	
	private Expression castEquivalent(CodePosition position, Expression value, StoredType toType) {
		if (toType.equals(value.type))
			return value;
		
		if (!(toType.type instanceof StringTypeID))
			System.out.println(position + ": " + value.type.getActualStorage() + " -> " + toType.getActualStorage());
		
		return new StorageCastExpression(position, value, toType);
	}
	
	public Expression castImplicit(CodePosition position, Expression value, StoredType toType, boolean implicit) {
		if (toType == null)
			throw new NullPointerException();
		
		toType = toType.getNormalized();
		if (toType.type == BasicTypeID.UNDETERMINED)
			return value;
		if (areEquivalent(type, toType))
			return castEquivalent(position, value, toType);
		
		if (type.type == BasicTypeID.NULL && toType.isOptional())
			return new NullExpression(position, toType);
		if (toType.isOptional() && canCastImplicit(toType.withoutOptional()))
			return castEquivalent(position, new WrapOptionalExpression(position, castImplicit(position, value, toType.withoutOptional(), implicit), toType), toType);
		if (type.isOptional() && areEquivalent(type.withoutOptional(), toType))
			return castEquivalent(position, new CheckNullExpression(position, value), toType);
		
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (caster.member.isImplicit() && areEquivalent(caster.member.toType, toType))
				return castEquivalent(position, caster.member.cast(position, value, implicit), toType);
		}
		for (TypeMember<ImplementationMemberRef> implementation : implementations) {
			if (implementation.member.implementsType.type.getNormalized() == toType.type)
				return castEquivalent(position, new InterfaceCastExpression(position, value, implementation.member), toType);
		}
		if (extendsType(toType.type))
			return new SupertypeCastExpression(position, value, toType);
		
		return new InvalidExpression(position, toType, CompileExceptionCode.INVALID_CAST, "Could not cast " + toString() + " to " + toType);
	}
	
	public Expression castExplicit(CodePosition position, Expression value, StoredType toType, boolean optional) {
		toType = toType.getNormalized();
		if (this.canCastImplicit(toType))
			return castImplicit(position, value, toType, false);
		
		for (TypeMember<CasterMemberRef> caster : casters)
			if (areEquivalent(caster.member.toType, toType))
				return castEquivalent(position, caster.member.cast(position, value, false), toType);
		
		return new InvalidExpression(position, toType, CompileExceptionCode.INVALID_CAST, "Cannot cast " + toString() + " to " + toType + ", even explicitly");
	}
	
	public boolean hasMember(String name) {
		return members.containsKey(name);
	}
	
	public IPartialExpression getMemberExpression(CodePosition position, TypeScope scope, Expression target, GenericName name, boolean allowStatic) {
		if (members.containsKey(name.name)) {
			TypeMemberGroup group = members.get(name.name);
			
			if (group.isStatic)
				return new PartialStaticMemberGroupExpression(position, scope, type.type, group, name.arguments);
			else
				return new PartialMemberGroupExpression(position, scope, target, group, name.arguments, allowStatic);
		}
		
		return null;
	}
	
	public IPartialExpression getStaticMemberExpression(CodePosition position, TypeScope scope, GenericName name) {
		if (members.containsKey(name.name))
			return new PartialStaticMemberGroupExpression(position, scope, type.type, members.get(name.name), name.arguments);
		if (innerTypes.containsKey(name.name))
			return new PartialTypeExpression(position, innerTypes.get(name.name).instance(cache.getRegistry(), name.arguments, (DefinitionTypeID)type.type), name.arguments);
		if (variantOptions.containsKey(name.name))
			return new PartialVariantOptionExpression(position, scope, variantOptions.get(name.name));
		
		return null;
	}
	
	public boolean hasInnerType(String name) {
		return innerTypes.containsKey(name);
	}
	
	public DefinitionTypeID getInnerType(CodePosition position, GenericName name) {
		if (!innerTypes.containsKey(name.name))
			throw new RuntimeException("No such inner type in " + type + ": " + name.name);
		
		return innerTypes.get(name.name).instance(cache.getRegistry(), name.arguments, (DefinitionTypeID)type.type);
	}
	
	@Override
	public String toString() {
		return type.toString();
	}
}
