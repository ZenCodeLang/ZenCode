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
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetStaticFieldExpression;
import org.openzen.zenscript.codemodel.expression.SetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticSetterExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.ICallableMember;
import org.openzen.zenscript.codemodel.member.IGettableMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionMemberGroup {
	public static DefinitionMemberGroup forMethod(ICallableMember member) {
		DefinitionMemberGroup instance = new DefinitionMemberGroup();
		instance.addMethod(member, TypeMemberPriority.SPECIFIED);
		return instance;
	}
	
	private TypeMember<FieldMember> field;
	private TypeMember<IGettableMember> getter;
	private TypeMember<SetterMember> setter;
	private final List<TypeMember<ICallableMember>> methods = new ArrayList<>();
	
	public void merge(CodePosition position, DefinitionMemberGroup other, TypeMemberPriority priority) {
		if (other.field != null)
			setField(other.field.member, priority);
		if (other.getter != null)
			setGetter(other.getter.member, priority);
		if (other.setter != null)
			setSetter(other.setter.member, priority);
		
		for (TypeMember<ICallableMember> method : other.methods)
			addMethod(method.member, priority);
	}
	
	public FieldMember getField() {
		return this.field == null ? null : this.field.member;
	}
	
	public IGettableMember getGetter() {
		return this.getter == null ? null : this.getter.member;
	}
	
	public SetterMember getSetter() {
		return this.setter == null ? null : this.setter.member;
	}
	
	public List<TypeMember<ICallableMember>> getMethodMembers() {
		return this.methods;
	}
	
	public void setField(FieldMember field, TypeMemberPriority priority) {
		if (this.field != null) {
			this.field = this.field.resolve(new TypeMember<>(priority, field));
		} else {
			this.field = new TypeMember<>(priority, field);
		}
	}
	
	public void setGetter(IGettableMember getter, TypeMemberPriority priority) {
		if (this.getter != null) {
			this.getter = this.getter.resolve(new TypeMember<>(priority, getter));
		} else {
			this.getter = new TypeMember<>(priority, getter);
		}
	}
	
	public void setSetter(SetterMember setter, TypeMemberPriority priority) {
		if (this.setter != null) {
			this.setter = this.setter.resolve(new TypeMember<>(priority, setter));
		} else {
			this.setter = new TypeMember<>(priority, setter);
		}
	}
	
	public void addMethod(ICallableMember method, TypeMemberPriority priority) {
		for (int i = 0; i < methods.size(); i++) {
			if (methods.get(i).member.getHeader().isEquivalentTo(method.getHeader())) {
				methods.set(i, methods.get(i).resolve(new TypeMember<>(priority, method)));
				return;
			}
		}
		
		methods.add(new TypeMember<>(priority, method));
	}
	
	public Expression getter(CodePosition position, Expression target, boolean allowStaticUsage) {
		if (getter != null) {
			if (getter.member.isStatic()) {
				if (!allowStaticUsage)
					throw new CompileException(position, CompileExceptionCode.USING_STATIC_ON_INSTANCE, "This field is static");
				
				return getter.member.getStatic(position);
			}
			
			return getter.member.get(position, target);
		} else if (field != null) {
			if (field.member.isStatic()) {
				if (!allowStaticUsage)
					throw new CompileException(position, CompileExceptionCode.USING_STATIC_ON_INSTANCE, "This field is static");
				
				return new GetStaticFieldExpression(position, field.member);
			}
			
			return new GetFieldExpression(position, target, field.member);
		} else {
			throw new CompileException(position, CompileExceptionCode.MEMBER_NO_GETTER, "Value is not a property");
		}
	}
	
	public Expression setter(CodePosition position, TypeScope scope, Expression target, Expression value, boolean allowStaticUsage) {
		if (setter != null) {
			if (setter.member.isStatic()) {
				if (!allowStaticUsage)
					throw new CompileException(position, CompileExceptionCode.USING_STATIC_ON_INSTANCE, "This field is static");
				
				return new StaticSetterExpression(position, setter.member, value.castImplicit(position, scope, setter.member.type));
			}
			
			return new SetterExpression(position, target, setter.member, value.castImplicit(position, scope, setter.member.type));
		} else if (field != null) {
			// TODO: perform proper checks on val fields
			//if (field.isFinal)
			//	throw new CompileException(position, "This field cannot be modified");
			if (field.member.isStatic()) {
				if (!allowStaticUsage)
					throw new CompileException(position, CompileExceptionCode.USING_STATIC_ON_INSTANCE, "This field is static");
				
				return new SetStaticFieldExpression(position, field.member, value.castImplicit(position, scope, field.member.type));
			}
			
			return new SetFieldExpression(position, target, field.member, value.castImplicit(position, scope, field.member.type));
		} else {
			throw new CompileException(position, CompileExceptionCode.MEMBER_NO_SETTER, "Value is not settable");
		}
	}
	
	public Expression staticGetter(CodePosition position) {
		if (getter != null) {
			if (!getter.member.isStatic())
				throw new CompileException(position, CompileExceptionCode.MEMBER_NOT_STATIC, "This getter is not static");
			
			return getter.member.getStatic(position);
		} else if (field != null) {
			if (!field.member.isStatic())
				throw new CompileException(position, CompileExceptionCode.MEMBER_NOT_STATIC, "This field is not static");
			
			return new GetStaticFieldExpression(position, field.member);
		} else {
			throw new CompileException(position, CompileExceptionCode.MEMBER_NO_GETTER, "Member is not gettable");
		}
	}
	
	public Expression staticSetter(CodePosition position, TypeScope scope, Expression value) {
		if (getter != null) {
			if (!getter.member.isStatic())
				throw new CompileException(position, CompileExceptionCode.MEMBER_NOT_STATIC, "This getter is not static");
			
			return new StaticSetterExpression(position, setter.member, value.castImplicit(position, scope, setter.member.type));
		} else if (field != null) {
			//if (field.member.isFinal)
			//	throw new CompileException(position, CompileExceptionCode.MEMBER_IS_FINAL, "This field cannot be modified");
			if (!field.member.isStatic())
				throw new CompileException(position, CompileExceptionCode.MEMBER_NOT_STATIC, "This field is not static");
			
			return new SetStaticFieldExpression(position, field.member, value.castImplicit(position, scope, field.member.type));
		} else {
			throw new CompileException(position, CompileExceptionCode.MEMBER_NO_SETTER, "Member is not settable");
		}
	}
	
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> typeHints, int arguments) {
		List<ITypeID>[] result = (List<ITypeID>[])(new List[arguments]);
		for (int i = 0; i < result.length; i++)
			result[i] = new ArrayList<>();
		
		for (TypeMember<ICallableMember> method : methods) {
			FunctionHeader header = method.member.getHeader();
			if (header.parameters.length != arguments)
				continue;
			
			if (header.typeParameters.length > 0) {
				for (ITypeID resultHint : typeHints) {
					Map<TypeParameter, ITypeID> mapping = new HashMap<>();
					if (header.returnType.inferTypeParameters(resultHint, mapping)) {
						header = header.withGenericArguments(scope.getTypeRegistry(), mapping);
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
	
	public Expression call(CodePosition position, TypeScope scope, Expression target, CallArguments arguments, boolean allowStaticUsage) {
		ICallableMember method = selectMethod(position, scope, arguments, true, allowStaticUsage);
		for (int i = 0; i < arguments.arguments.length; i++) {
			arguments.arguments[i] = arguments.arguments[i].castImplicit(position, scope, method.getHeader().parameters[i].type);
		}
		
		FunctionHeader instancedHeader = method.getHeader().withGenericArguments(scope.getTypeRegistry(), arguments.typeArguments);
		return method.call(position, target, instancedHeader, arguments);
	}
	
	public Expression callWithComparator(
			CodePosition position,
			TypeScope scope,
			Expression target,
			CallArguments arguments,
			CompareType compareType) {
		ICallableMember method = selectMethod(position, scope, arguments, true, false);
		FunctionHeader instancedHeader = method.getHeader().withGenericArguments(scope.getTypeRegistry(), arguments.typeArguments);
		return method.callWithComparator(position, compareType, target, instancedHeader, arguments);
	}
	
	public Expression callStatic(CodePosition position, ITypeID target, TypeScope scope, CallArguments arguments) {
		ICallableMember method = selectMethod(position, scope, arguments, false, true);
		FunctionHeader instancedHeader = method.getHeader().withGenericArguments(scope.getTypeRegistry(), arguments.typeArguments);
		return method.callStatic(position, target, instancedHeader, arguments);
	}
	
	public Expression callStaticWithComparator(
			CodePosition position,
			ITypeID target, 
			TypeScope scope,
			CallArguments arguments,
			CompareType compareType) {
		ICallableMember method = selectMethod(position, scope, arguments, false, true);
		FunctionHeader instancedHeader = method.getHeader().withGenericArguments(scope.getTypeRegistry(), arguments.typeArguments);
		return method.callStaticWithComparator(position, target, compareType, instancedHeader, arguments);
	}
	
	public ICallableMember selectMethod(CodePosition position, TypeScope scope, CallArguments arguments, boolean allowNonStatic, boolean allowStatic) {
		// try to match with exact types
		outer: for (TypeMember<ICallableMember> method : methods) {
			if (!(method.member.isStatic() ? allowStatic : allowNonStatic))
				continue;
			
			FunctionHeader header = method.member.getHeader();
			if (header.parameters.length != arguments.arguments.length)
				continue;
			if (header.typeParameters.length != arguments.typeArguments.length)
				continue;
			
			if (arguments.typeArguments.length > 0) {
				header = header.withGenericArguments(scope.getTypeRegistry(), arguments.typeArguments);
			}
			
			for (int i = 0; i < header.parameters.length; i++) {
				if (arguments.arguments[i].getType() != header.parameters[i].type)
					continue outer;
			}
			
			return method.member;
		}
		
		// try to match with approximate types
		ICallableMember selected = null;
		outer: for (TypeMember<ICallableMember> method : methods) {
			if (!(method.member.isStatic() ? allowStatic : allowNonStatic))
				continue;
			
			FunctionHeader header = method.member.getHeader();
			if (header.parameters.length != arguments.arguments.length)
				continue;
			if (header.typeParameters.length != arguments.typeArguments.length)
				continue;
			
			if (arguments.typeArguments.length > 0) {
				header = header.withGenericArguments(scope.getTypeRegistry(), arguments.typeArguments);
			}
			
			for (int i = 0; i < header.parameters.length; i++) {
				if (!scope.getTypeMembers(arguments.arguments[i].getType()).canCastImplicit(header.parameters[i].type))
					continue outer;
			}
			
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
			outer: for (TypeMember<ICallableMember> method : methods) {
				if (!(method.member.isStatic() ? allowStatic : allowNonStatic)) {
					message.append(method.member.isStatic() ? "Method must not be static" : "Method must be static").append('\n');
					continue;
				}
				
				message.append(method.member.getHeader().explainWhyIncompatible(scope, arguments)).append("\n");
			}
			
			throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "No matching method found:\n" + message.toString());
		}
		
		return selected;
	}
}
