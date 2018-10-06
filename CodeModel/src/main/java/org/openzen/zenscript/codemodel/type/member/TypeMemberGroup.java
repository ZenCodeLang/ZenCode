/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticSetterExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.ConstExpression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.PostCallExpression;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.ref.ConstMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.SetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeMemberGroup {
	public static TypeMemberGroup forMethod(String name, FunctionalMemberRef member) {
		TypeMemberGroup instance = new TypeMemberGroup(member.isStatic(), name);
		instance.addMethod(member, TypeMemberPriority.SPECIFIED);
		return instance;
	}
	
	private TypeMember<ConstMemberRef> constant;
	private TypeMember<FieldMemberRef> field;
	private TypeMember<GetterMemberRef> getter;
	private TypeMember<SetterMemberRef> setter;
	private final List<TypeMember<FunctionalMemberRef>> methods = new ArrayList<>();
	public final boolean isStatic;
	public final String name;
	
	public TypeMemberGroup(boolean isStatic, String name) {
		this.isStatic = isStatic;
		this.name = name;
	}
	
	public void merge(CodePosition position, TypeMemberGroup other, TypeMemberPriority priority) {
		if (other.constant != null)
			setConst(other.constant.member, priority);
		if (other.field != null)
			setField(other.field.member, priority);
		if (other.getter != null)
			setGetter(other.getter.member, priority);
		if (other.setter != null)
			setSetter(other.setter.member, priority);
		
		for (TypeMember<FunctionalMemberRef> method : other.methods)
			addMethod(method.member, priority);
	}
	
	public FieldMemberRef getField() {
		return this.field == null ? null : this.field.member;
	}
	
	public GetterMemberRef getGetter() {
		return this.getter == null ? null : this.getter.member;
	}
	
	public SetterMemberRef getSetter() {
		return this.setter == null ? null : this.setter.member;
	}
	
	public FunctionalMemberRef getUnaryMethod() {
		for (TypeMember<FunctionalMemberRef> method : methods)
			if (method.member.getHeader().parameters.length == 0)
				return method.member;
		
		return null;
	}
	
	public boolean hasMethods() {
		return !methods.isEmpty();
	}
	
	public boolean hasMethod(FunctionHeader header) {
		for (TypeMember<FunctionalMemberRef> method : methods) {
			if (method.member.getHeader().isEquivalentTo(header))
				return true;
		}
		
		return false;
	}
	
	public FunctionalMemberRef getStaticMethod(int arguments, StoredType returnType) {
		for (TypeMember<FunctionalMemberRef> method : methods) {
			if (method.member.isStatic() && method.member.getHeader().accepts(arguments) && method.member.getHeader().getReturnType().equals(returnType))
				return method.member;
		}
		
		return null;
	}
	
	public List<TypeMember<FunctionalMemberRef>> getMethodMembers() {
		return this.methods;
	}
	
	public ConstMemberRef getConstant() {
		return constant.member;
	}
	
	public void setConst(ConstMemberRef constant, TypeMemberPriority priority) {
		if (this.constant != null) {
			this.constant = this.constant.resolve(new TypeMember<>(priority, constant));
		} else {
			this.constant = new TypeMember<>(priority, constant);
		}
	}
	
	public void setField(FieldMemberRef field, TypeMemberPriority priority) {
		if (this.field != null) {
			this.field = this.field.resolve(new TypeMember<>(priority, field));
		} else {
			this.field = new TypeMember<>(priority, field);
		}
	}
	
	public void setGetter(GetterMemberRef getter, TypeMemberPriority priority) {
		if (this.getter != null) {
			this.getter = this.getter.resolve(new TypeMember<>(priority, getter));
		} else {
			this.getter = new TypeMember<>(priority, getter);
		}
	}
	
	public void setSetter(SetterMemberRef setter, TypeMemberPriority priority) {
		if (this.setter != null) {
			this.setter = this.setter.resolve(new TypeMember<>(priority, setter));
		} else {
			this.setter = new TypeMember<>(priority, setter);
		}
	}
	
	public void addMethod(FunctionalMemberRef method, TypeMemberPriority priority) {
		methods.add(new TypeMember<>(priority, method));
	}
	
	public Expression getter(CodePosition position, TypeScope scope, Expression target, boolean allowStaticUsage) throws CompileException {
		if (getter != null) {
			if (getter.member.isStatic()) {
				if (!allowStaticUsage)
					return new InvalidExpression(position, getter.member.getType(), CompileExceptionCode.USING_STATIC_ON_INSTANCE, "This field is static");

				return getter.member.getStatic(position);
			}

			scope.getPreparer().prepare(getter.member.member);
			return getter.member.get(position, target);
		} else if (field != null) {
			if (field.member.isStatic()) {
				if (!allowStaticUsage)
					return new InvalidExpression(position, field.member.getType(), CompileExceptionCode.USING_STATIC_ON_INSTANCE, "This field is static");

				return new GetStaticFieldExpression(position, field.member);
			}

			scope.getPreparer().prepare(field.member.member);
			return new GetFieldExpression(position, target, field.member);
		} else {
			throw new CompileException(position, CompileExceptionCode.MEMBER_NO_GETTER, "Value is not a property");
		}
	}
	
	public Expression setter(CodePosition position, TypeScope scope, Expression target, Expression value, boolean allowStaticUsage) throws CompileException {
		if (setter != null) {
			if (setter.member.isStatic()) {
				if (!allowStaticUsage)
					return new InvalidExpression(position, setter.member.getType(), CompileExceptionCode.USING_STATIC_ON_INSTANCE, "This field is static");

				scope.getPreparer().prepare(setter.member.member);
				return new StaticSetterExpression(position, setter.member, value.castImplicit(position, scope, setter.member.getType()));
			}

			scope.getPreparer().prepare(setter.member.member);
			return new SetterExpression(position, target, setter.member, value.castImplicit(position, scope, setter.member.getType()));
		} else if (field != null) {
			// TODO: perform proper checks on val fields
			//if (field.isFinal)
			//	throw new CompileException(position, "This field cannot be modified");
			if (field.member.isStatic()) {
				if (!allowStaticUsage)
					return new InvalidExpression(position, field.member.getType(), CompileExceptionCode.USING_STATIC_ON_INSTANCE, "This field is static");

				scope.getPreparer().prepare(field.member.member);
				return new SetStaticFieldExpression(position, field.member, value.castImplicit(position, scope, field.member.getType()));
			}

			scope.getPreparer().prepare(field.member.member);
			return new SetFieldExpression(position, target, field.member, value.castImplicit(position, scope, field.member.getType()));
		} else {
			throw new CompileException(position, CompileExceptionCode.MEMBER_NO_SETTER, "Value is not settable");
		}
	}
	
	public Expression staticGetter(CodePosition position, TypeScope scope) throws CompileException {
		if (constant != null) {
			return new ConstExpression(position, constant.member);
		} else if (getter != null) {
			if (!getter.member.isStatic())
				return new InvalidExpression(position, getter.member.getType(), CompileExceptionCode.MEMBER_NOT_STATIC, "This getter is not static");

			scope.getPreparer().prepare(getter.member.member);
			return getter.member.getStatic(position);
		} else if (field != null) {
			if (!field.member.isStatic())
				return new InvalidExpression(position, field.member.getType(), CompileExceptionCode.MEMBER_NOT_STATIC, "This field is not static");

			scope.getPreparer().prepare(field.member.member);
			return new GetStaticFieldExpression(position, field.member);
		} else {
			throw new CompileException(position, CompileExceptionCode.MEMBER_NO_GETTER, "Member is not gettable");
		}
	}
	
	public Expression staticSetter(CodePosition position, TypeScope scope, Expression value) throws CompileException {
		if (getter != null) {
			if (!getter.member.isStatic())
				return new InvalidExpression(position, getter.member.getType(), CompileExceptionCode.MEMBER_NOT_STATIC, "This getter is not static");

			scope.getPreparer().prepare(setter.member.member);
			return new StaticSetterExpression(position, setter.member, value.castImplicit(position, scope, setter.member.getType()));
		} else if (field != null) {
			//if (field.member.isFinal)
			//	throw new CompileException(position, CompileExceptionCode.MEMBER_IS_FINAL, "This field cannot be modified");
			if (!field.member.isStatic())
				return new InvalidExpression(position, field.member.getType(), CompileExceptionCode.MEMBER_NOT_STATIC, "This field is not static");

			scope.getPreparer().prepare(field.member.member);
			return new SetStaticFieldExpression(position, field.member, value.castImplicit(position, scope, field.member.getType()));
		} else {
			throw new CompileException(position, CompileExceptionCode.MEMBER_NO_SETTER, "Member is not settable");
		}
	}
	
	public List<StoredType>[] predictCallTypes(TypeScope scope, List<StoredType> typeHints, int arguments) {
		List<StoredType>[] result = (List<StoredType>[])(new List[arguments]);
		for (int i = 0; i < result.length; i++)
			result[i] = new ArrayList<>();
		
		for (TypeMember<FunctionalMemberRef> method : methods) {
			FunctionHeader header = method.member.getHeader();
			if (header.parameters.length != arguments)
				continue;
			
			if (header.typeParameters != null) {
				for (StoredType resultHint : typeHints) {
					Map<TypeParameter, StoredType> mapping = header.getReturnType().inferTypeParameters(scope.getMemberCache(), resultHint);
					if (mapping != null) {
						header = header.withGenericArguments(scope.getTypeRegistry(), scope.getLocalTypeParameters().getInner(scope.getTypeRegistry(), mapping));
						break;
					}
				}
			}
			
			for (int i = 0; i < header.parameters.length; i++) {
				if (!result[i].contains(header.parameters[i].type))
					result[i].add(header.parameters[i].type);
			}
		}
		
		return result;
	}
	
	public Expression call(CodePosition position, TypeScope scope, Expression target, CallArguments arguments, boolean allowStaticUsage) throws CompileException {
		FunctionalMemberRef method = selectMethod(position, scope, arguments, true, allowStaticUsage);
		FunctionHeader instancedHeader = method.getHeader().fillGenericArguments(scope.getTypeRegistry(), arguments.typeArguments, scope.getLocalTypeParameters());
		for (int i = 0; i < arguments.arguments.length; i++) {
			arguments.arguments[i] = arguments.arguments[i].castImplicit(position, scope, instancedHeader.parameters[i].type);
		}

		scope.getPreparer().prepare(method.getTarget());
		return method.call(position, target, instancedHeader, arguments, scope);
	}
	
	public Expression callPostfix(CodePosition position, TypeScope scope, Expression target) throws CompileException {
		if (methods.isEmpty())
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "There is no such operator");
		
		FunctionalMemberRef method = methods.get(0).member;
		if (!method.isOperator())
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Member is not an operator");
		
		scope.getPreparer().prepare(method.getTarget());
		return new PostCallExpression(position, target, method, method.getHeader());
	}
	
	public Expression callWithComparator(
			CodePosition position,
			TypeScope scope,
			Expression target,
			CallArguments arguments,
			CompareType compareType) throws CompileException
	{
		FunctionalMemberRef method = selectMethod(position, scope, arguments, true, false);
		FunctionHeader instancedHeader = method.getHeader().fillGenericArguments(scope.getTypeRegistry(), arguments.typeArguments, scope.getLocalTypeParameters());
		return method.callWithComparator(position, compareType, target, instancedHeader, arguments, scope);
	}
	
	public Expression callStatic(CodePosition position, TypeID target, TypeScope scope, CallArguments arguments) throws CompileException {
		FunctionalMemberRef method = selectMethod(position, scope, arguments, false, true);
		FunctionHeader instancedHeader = method.getHeader().fillGenericArguments(scope.getTypeRegistry(), arguments.typeArguments, scope.getLocalTypeParameters());
		return method.callStatic(position, target, instancedHeader, arguments, scope);
	}
	
	public FunctionalMemberRef selectMethod(CodePosition position, TypeScope scope, CallArguments arguments, boolean allowNonStatic, boolean allowStatic) throws CompileException {
		// try to match with exact types
		for (TypeMember<FunctionalMemberRef> method : methods) {
			if (!(method.member.isStatic() ? allowStatic : allowNonStatic))
				continue;
			
			FunctionHeader header = method.member.getHeader();
			if (header.matchesExactly(arguments, scope))
				return method.member;
		}
		
		// try to match with approximate types
		FunctionalMemberRef selected = null;
		for (TypeMember<FunctionalMemberRef> method : methods) {
			if (!(method.member.isStatic() ? allowStatic : allowNonStatic))
				continue;
			if (arguments.arguments.length < method.member.getHeader().minParameters || arguments.arguments.length > method.member.getHeader().maxParameters)
				continue;
			
			scope.getPreparer().prepare(method.member.getTarget());
			
			FunctionHeader header = method.member.getHeader();
			if (!header.matchesImplicitly(arguments, scope))
				continue;
			
			if (selected != null) {
				StringBuilder explanation = new StringBuilder();
				explanation.append("Function A: ").append(selected.getHeader().toString()).append("\n");
				explanation.append("Function B: ").append(method.member.getHeader().toString());
				throw new CompileException(position, CompileExceptionCode.CALL_AMBIGUOUS, "Ambiguous call; multiple methods match:\n" + explanation.toString());
			}
			
			selected = method.member;
		}
		
		if (selected == null) {
			// let's figure out why this didn't work out
			StringBuilder message = new StringBuilder();
			if (methods.isEmpty()) {
				throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "This type has no " + name);
			}
			
			for (TypeMember<FunctionalMemberRef> method : methods) {
				if (!(method.member.isStatic() ? allowStatic : allowNonStatic)) {
					message.append(method.member.isStatic() ? "Method must not be static" : "Method must be static").append('\n');
					continue;
				}
				
				message.append(method.member.getHeader().explainWhyIncompatible(scope, arguments)).append("\n");
			}
			
			throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "No matching method found for " + name + ":\n" + message.toString());
		}
		
		return selected;
	}
	
	public FunctionalMemberRef getOverride(CodePosition position, TypeScope scope, FunctionalMember member) throws CompileException {
		List<FunctionalMemberRef> candidates = new ArrayList<>();
		for (TypeMember<FunctionalMemberRef> method : methods) {
			if (member.header.canOverride(scope, method.member.getHeader()))
				candidates.add(method.member);
		}
		
		if (candidates.isEmpty())
			return null;
		if (candidates.size() == 1)
			return candidates.get(0);
		
		throw new CompileException(position, CompileExceptionCode.OVERRIDE_AMBIGUOUS, "Ambiguous override: has " + candidates.size() + " base candidates");
	}
}
