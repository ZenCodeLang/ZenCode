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
import org.openzen.zenscript.codemodel.expression.MakeConstExpression;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.expression.SupertypeCastExpression;
import org.openzen.zenscript.codemodel.expression.WrapOptionalExpression;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.InnerDefinition;
import org.openzen.zenscript.codemodel.member.ref.CasterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.ConstMemberRef;
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
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public final class TypeMembers {
	private final LocalMemberCache cache;
	public final ITypeID type;
	
	private final List<TypeMember<CasterMemberRef>> casters = new ArrayList<>();
	private final List<TypeMember<ImplementationMemberRef>> implementations = new ArrayList<>();
	private final List<TypeMember<IteratorMemberRef>> iterators = new ArrayList<>();
	
	private final Map<String, EnumConstantMember> enumMembers = new HashMap<>();
	private final Map<String, VariantOptionRef> variantOptions = new HashMap<>();
	private final Map<String, DefinitionMemberGroup> members = new HashMap<>();
	private final Map<String, InnerDefinition> innerTypes = new HashMap<>();
	private final Map<OperatorType, DefinitionMemberGroup> operators = new HashMap<>();
	
	public TypeMembers(LocalMemberCache cache, ITypeID type) {
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
	
	public boolean extendsOrImplements(ITypeID other) {
		other = other.getNormalized();
		ITypeID superType = type.getSuperType(cache.getRegistry());
		if (superType != null) {
			if (superType == other)
				return true;
			if (cache.get(superType).extendsOrImplements(other))
				return true;
		}
		
		for (TypeMember<ImplementationMemberRef> implementation : implementations) {
			if (implementation.member.implementsType.equals(other)) // TODO: for some reason duplicate types are generated
				return true;
			if (cache.get(implementation.member.implementsType).extendsOrImplements(other))
				return true;
		}
		
		return false;
	}
	
	public boolean extendsType(ITypeID other) {
		other = other.getNormalized();
		ITypeID superType = type.getSuperType(cache.getRegistry());
		if (superType != null) {
			if (superType == other)
				return true;
			if (cache.get(superType).extendsType(other))
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
		for (Map.Entry<String, DefinitionMemberGroup> entry : members.entrySet())
			other.getOrCreateGroup(entry.getKey(), entry.getValue().isStatic).merge(position, entry.getValue(), priority);
		for (Map.Entry<String, InnerDefinition> entry : innerTypes.entrySet())
			other.innerTypes.put(entry.getKey(), entry.getValue());
		for (Map.Entry<OperatorType, DefinitionMemberGroup> entry : operators.entrySet())
			other.getOrCreateGroup(entry.getKey()).merge(position, entry.getValue(), priority);
	}
	
	public ITypeID union(ITypeID other) {
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
		for (Map.Entry<String, DefinitionMemberGroup> entry : members.entrySet()) {
			DefinitionMemberGroup group = entry.getValue();
			if (group.getGetter() != null && group.getGetter().member.isAbstract() && !implemented.contains(group.getGetter().member))
				result.add(group.getGetter().member);
			if (group.getSetter() != null && group.getSetter().member.isAbstract() && !implemented.contains(group.getSetter().member))
				result.add(group.getSetter().member);
			for (TypeMember<FunctionalMemberRef> member : group.getMethodMembers())
				if (member.member.getTarget().isAbstract() && !implemented.contains(member.member.getTarget()))
					result.add(member.member.getTarget());
		}
		for (Map.Entry<OperatorType, DefinitionMemberGroup> entry : operators.entrySet()) {
			if (entry.getKey() == OperatorType.DESTRUCTOR)
				continue; // destructor doesn't have to be implemented; the compiler can do so automatically
			
			DefinitionMemberGroup group = entry.getValue();
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
	
	public void addCaster(CasterMemberRef caster) {
		addCaster(caster, TypeMemberPriority.SPECIFIED);
	}
	
	public void addCaster(CasterMemberRef caster, TypeMemberPriority priority) {
		for (int i = 0; i < casters.size(); i++) {
			if (casters.get(i).member.toType == caster.toType) {
				casters.set(i, casters.get(i).resolve(new TypeMember<>(priority, caster)));
				return;
			}
		}
		
		casters.add(new TypeMember<>(priority, caster));
	}
	
	public void addConst(ConstMemberRef member) {
		DefinitionMemberGroup group = getOrCreateGroup(member.member.name, true);
		group.setConst(member, TypeMemberPriority.SPECIFIED);
	}
	
	public void addField(FieldMemberRef member) {
		addField(member, TypeMemberPriority.SPECIFIED);
	}
	
	public void addField(FieldMemberRef member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setField(member, priority);
	}
	
	public void addGetter(GetterMemberRef member) {
		addGetter(member, TypeMemberPriority.SPECIFIED);
	}
	
	public void addGetter(GetterMemberRef member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setGetter(member, priority);
	}
	
	public void addSetter(SetterMemberRef member) {
		addSetter(member, TypeMemberPriority.SPECIFIED);
	}
	
	public void addSetter(SetterMemberRef member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(member.member.name, member.isStatic());
		group.setSetter(member, priority);
	}
	
	public void addMethod(String name, FunctionalMemberRef member) {
		addMethod(name, member, TypeMemberPriority.SPECIFIED);
	}
	
	public void addMethod(String name, FunctionalMemberRef member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(name, member.isStatic());
		group.addMethod(member, priority);
	}
	
	public void addOperator(OperatorType operator, FunctionalMemberRef member) {
		addOperator(operator, member, TypeMemberPriority.SPECIFIED);
	}
	
	public boolean hasOperator(OperatorType operator) {
		return operators.containsKey(operator) && operators.get(operator).hasMethods();
	}
	
	public void addOperator(OperatorType operator, FunctionalMemberRef member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(operator);
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
	
	public DefinitionMemberGroup getOrCreateGroup(String name, boolean isStatic) {
		if (!members.containsKey(name))
			members.put(name, new DefinitionMemberGroup(isStatic, name));
		
		return members.get(name);
	}
	
	public DefinitionMemberGroup getOrCreateGroup(OperatorType operator) {
		if (!operators.containsKey(operator))
			operators.put(operator, new DefinitionMemberGroup(false, operator.operator + " operator"));
		
		return operators.get(operator);
	}
	
	public void addEnumMember(EnumConstantMember member, TypeMemberPriority priority) {
		if (enumMembers.containsKey(member.name))
			throw new CompileException(member.position, CompileExceptionCode.ENUM_VALUE_DUPLICATE, "Duplicate enum member " + member.name);
		
		enumMembers.put(member.name, member);
	}
	
	public EnumConstantMember getEnumMember(String name) {
		return enumMembers.get(name);
	}
	
	public VariantOptionRef getVariantOption(String name) {
		return variantOptions.get(name);
	}
	
	public Expression compare(CodePosition position, TypeScope scope, CompareType operator, Expression left, Expression right) {
		if (operator == CompareType.EQ) {
			DefinitionMemberGroup equal = getOrCreateGroup(OperatorType.EQUALS);
			for (TypeMember<FunctionalMemberRef> member : equal.getMethodMembers()) {
				if (member.member.getHeader().accepts(scope, right))
					return equal.call(position, scope, left, new CallArguments(right), false);
			}
		} else if (operator == CompareType.NE) {
			DefinitionMemberGroup equal = getOrCreateGroup(OperatorType.NOTEQUALS);
			for (TypeMember<FunctionalMemberRef> member : equal.getMethodMembers()) {
				if (member.member.getHeader().accepts(scope, right)) {
					return equal.call(position, scope, left, new CallArguments(right), false);
				}
			}
		}
		
		DefinitionMemberGroup compare = getOrCreateGroup(OperatorType.COMPARE);
		return compare.callWithComparator(position, scope, left, new CallArguments(right), operator);
	}
	
	public Expression unary(CodePosition position, TypeScope scope, OperatorType operator, Expression value) {
		DefinitionMemberGroup members = getOrCreateGroup(operator);
		return members.call(position, scope, value, new CallArguments(Expression.NONE), false);
	}
	
	public IteratorMemberRef getIterator(int variables) {
		for (TypeMember<IteratorMemberRef> iterator : iterators)
			if (iterator.member.getLoopVariableCount() == variables)
				return iterator.member;
		
		return null;
	}
	
	public ITypeID[] getLoopTypes(int variables) {
		for (TypeMember<IteratorMemberRef> iterator : iterators)
			if (iterator.member.getLoopVariableCount() == variables)
				return iterator.member.types;
		
		return null;
	}
	
	public boolean canCastImplicit(ITypeID toType) {
		toType = toType.getNormalized();
		if (toType == type)
			return true;
		if (toType == null)
			throw new NullPointerException();
		if (toType == BasicTypeID.UNDETERMINED)
			throw new IllegalArgumentException("Cannot cast to undetermined type!");
		
		if (type == BasicTypeID.NULL && toType.isOptional())
			return true;
		if (toType.isOptional() && canCastImplicit(toType.withoutOptional()))
			return true;
		if (toType.isConst() && canCastImplicit(toType.withoutConst()))
			return true;
		if (type.isOptional() && type.withoutOptional() == toType)
			return true;
		
		return getImplicitCaster(toType) != null || extendsOrImplements(toType);
	}
	
	public CasterMemberRef getImplicitCaster(ITypeID toType) {
		toType = toType.getNormalized();
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (caster.member.isImplicit() && toType == caster.member.toType)
				return caster.member;
		}
		
		return null;
	}
	
	public CasterMemberRef getCaster(ITypeID toType) {
		toType = toType.getNormalized();
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (toType == caster.member.toType)
				return caster.member;
		}
		
		return null;
	}
	
	public boolean canCast(ITypeID toType) {
		toType = toType.getNormalized();
		if (canCastImplicit(toType))
			return true;
		
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (toType == caster.member.toType)
				return true;
		}
		
		return false;
	}
	
	public Expression castImplicit(CodePosition position, Expression value, ITypeID toType, boolean implicit) {
		toType = toType.getNormalized();
		if (toType == type || toType == BasicTypeID.UNDETERMINED)
			return value;
		if (toType == null)
			throw new NullPointerException();
		
		if (type == BasicTypeID.NULL && toType.isOptional())
			return new NullExpression(position, toType);
		if (toType.isOptional() && canCastImplicit(toType.withoutOptional()))
			return new WrapOptionalExpression(position, castImplicit(position, value, toType.withoutOptional(), implicit), toType);
		if (toType.isConst() && canCastImplicit(toType.withoutConst()))
			return new MakeConstExpression(position, castImplicit(position, value, toType.withoutConst(), implicit), toType);
		if (type.isOptional() && type.withoutOptional() == toType)
			return new CheckNullExpression(position, value);
		
		for (TypeMember<CasterMemberRef> caster : casters) {
			if (caster.member.isImplicit() && toType == caster.member.toType)
				return caster.member.cast(position, value, implicit);
		}
		for (TypeMember<ImplementationMemberRef> implementation : implementations) {
			if (implementation.member.implementsType.equals(toType))
				return new InterfaceCastExpression(position, value, toType);
		}
		if (extendsType(toType))
			return new SupertypeCastExpression(position, value, toType);
		
		throw new CompileException(position, CompileExceptionCode.INVALID_CAST, "Could not cast " + toString() + " to " + toType);
	}
	
	public Expression castExplicit(CodePosition position, Expression value, ITypeID toType, boolean optional) {
		toType = toType.getNormalized();
		if (this.canCastImplicit(toType))
			return castImplicit(position, value, toType, false);
		
		for (TypeMember<CasterMemberRef> caster : casters)
			if (toType == caster.member.toType)
				return caster.member.cast(position, value, false);
		
		throw new CompileException(position, CompileExceptionCode.INVALID_CAST, "Cannot cast " + toString() + " to " + toType + ", even explicitly");
	}
	
	public boolean hasMember(String name) {
		return members.containsKey(name);
	}
	
	public IPartialExpression getMemberExpression(CodePosition position, TypeScope scope, Expression target, GenericName name, boolean allowStatic) {
		if (members.containsKey(name.name)) {
			DefinitionMemberGroup group = members.get(name.name);
			
			if (group.isStatic)
				return new PartialStaticMemberGroupExpression(position, scope, type, group, name.arguments);
			else
				return new PartialMemberGroupExpression(position, scope, target, group, name.arguments, allowStatic);
		}
		
		return null;
	}
	
	public IPartialExpression getStaticMemberExpression(CodePosition position, TypeScope scope, GenericName name) {
		if (members.containsKey(name.name))
			return new PartialStaticMemberGroupExpression(position, scope, type, members.get(name.name), name.arguments);
		if (innerTypes.containsKey(name.name))
			return new PartialTypeExpression(position, innerTypes.get(name.name).instance(cache.getRegistry(), name.arguments, (DefinitionTypeID)type), name.arguments);
		if (variantOptions.containsKey(name.name))
			return new PartialVariantOptionExpression(position, scope, variantOptions.get(name.name));
		
		return null;
	}
	
	public boolean hasInnerType(String name) {
		return innerTypes.containsKey(name);
	}
	
	public DefinitionTypeID getInnerType(CodePosition position, GenericName name) {
		if (!innerTypes.containsKey(name.name))
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_INNER_TYPE, "No such inner type in " + type + ": " + name.name);
		
		return innerTypes.get(name.name).instance(cache.getRegistry(), name.arguments, (DefinitionTypeID)type);
	}
	
	@Override
	public String toString() {
		return type.toString();
	}
}
