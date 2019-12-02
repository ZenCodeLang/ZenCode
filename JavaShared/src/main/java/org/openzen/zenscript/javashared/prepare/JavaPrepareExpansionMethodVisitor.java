/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared.prepare;

import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.javashared.JavaNativeClass;
import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.javashared.JavaTypeNameVisitor;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaImplementation;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaModifiers;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaPrepareExpansionMethodVisitor implements MemberVisitor<Void> {
	private static final boolean DEBUG_EMPTY = true;
	
	private final JavaContext context;
	private final JavaCompiledModule module;
	private final JavaClass cls;
	private final JavaNativeClass nativeClass;
	
	public JavaPrepareExpansionMethodVisitor(JavaContext context, JavaCompiledModule module, JavaClass cls, JavaNativeClass nativeClass) {
		this.module = module;
		this.cls = cls;
		this.nativeClass = nativeClass;
		this.context = context;
		cls.empty = true;
	}
	
	@Override
	public Void visitConst(ConstMember member) {
		JavaField field = new JavaField(cls, member.name, context.getDescriptor(member.getType()));
		module.setFieldInfo(member, field);
		
		if (DEBUG_EMPTY && cls.empty)
			System.out.println("Class " + cls.fullName + " not empty because of const");
		
		cls.empty = false;
		return null;
	}
	
	@Override
	public Void visitField(FieldMember member) {
		// TODO: expansion fields
		JavaField field = new JavaField(cls, member.name, context.getDescriptor(member.getType()));
		module.setFieldInfo(member, field);
		
		if (member.hasAutoGetter() || member.hasAutoSetter())
			cls.empty = false;
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		visitFunctional(member, member.header, "");
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		if (nativeClass != null && nativeClass.nonDestructible)
			return null;
		
		visitFunctional(member, member.header, "");
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		visitFunctional(member, member.header, member.name);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		visitFunctional(member, new FunctionHeader(member.getType()), "get" + StringExpansion.capitalize(member.name));
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		visitFunctional(member, new FunctionHeader(BasicTypeID.VOID, member.getType()), "set" + StringExpansion.capitalize(member.name));
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		visitFunctional(member, member.header, getOperatorName(member.operator));
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		visitFunctional(member, member.header, "to" + JavaTypeNameVisitor.INSTANCE.process(member.toType));
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		visitFunctional(member, member.header, member.getLoopVariableCount() == 1 ? "iterator" : "iterator" + member.getLoopVariableCount());
		return null;
	}

	@Override
	public Void visitCaller(CallerMember member) {
		visitFunctional(member, member.header, "call");
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		JavaClass implementationClass = new JavaClass(cls, JavaTypeNameVisitor.INSTANCE.process(member.type) + "Implementation", JavaClass.Kind.CLASS);
		module.setImplementationInfo(member, new JavaImplementation(false, implementationClass));
		for (IDefinitionMember implementedMember : member.members)
			implementedMember.accept(this);
		
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		// TODO
		cls.empty = false;
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		cls.empty = false;
		return null;
	}
	
	private void visitFunctional(DefinitionMember member, FunctionHeader header, String name) {
		NativeTag nativeTag = member.getTag(NativeTag.class);
		JavaMethod method = null;
		if (nativeTag != null && nativeClass != null)
			method = nativeClass.getMethod(nativeTag.value);
		if (method == null) {
			final JavaMethod.Kind kind = getKind(member);
			final String descriptor;
			if (kind == JavaMethod.Kind.EXPANSION && member.definition instanceof ExpansionDefinition) {
				descriptor = context.getMethodDescriptorExpansion(header, ((ExpansionDefinition) member.definition).target);
			} else {
				descriptor = context.getMethodDescriptor(header);
			}
			method = new JavaMethod(
					cls,
					kind,
					name,
					true,
					descriptor,
					JavaModifiers.getJavaModifiers(member.getEffectiveModifiers()),
					header.getReturnType().type instanceof GenericTypeID,
					header.useTypeParameters());
		}
		
		if (method.compile) {
			if (DEBUG_EMPTY && cls.empty)
				System.out.println("Class " + cls.fullName + " not empty because of " + member.describe());
			
			cls.empty = false;
		}
		
		module.setMethodInfo(member, method);
	}
	
	private JavaMethod.Kind getKind(DefinitionMember member) {
		return member.isStatic() ? JavaMethod.Kind.STATIC : JavaMethod.Kind.EXPANSION;
	}
	
	private String getOperatorName(OperatorType operator) {
		switch (operator) {
			case NEG:
				return "negate";
			case NOT:
				return "invert";
			case ADD:
				return "add";
			case ADDASSIGN:
				return "addAssign";
			case SUB:
				return "subtract";
			case SUBASSIGN:
				return "subAssign";
			case CAT:
				return "concat";
			case CATASSIGN:
				return "append";
			case MUL:
				return "mul";
			case MULASSIGN:
				return "mulAssign";
			case DIV:
				return "div";
			case DIVASSIGN:
				return "divAssign";
			case MOD:
				return "mod";
			case MODASSIGN:
				return "modAssign";
			case AND:
				return "and";
			case ANDASSIGN:
				return "andAssign";
			case OR:
				return "or";
			case ORASSIGN:
				return "orAssign";
			case XOR:
				return "xor";
			case XORASSIGN:
				return "xorAssign";
			case SHL:
				return "shl";
			case SHLASSIGN:
				return "shlAssign";
			case SHR:
				return "shr";
			case SHRASSIGN:
				return "shrAssign";
			case USHR:
				return "ushr";
			case USHRASSIGN:
				return "ushrAssign";
			case INDEXGET:
				return "getAt";
			case INDEXSET:
				return "setAt";
			case INCREMENT:
				return "increment";
			case DECREMENT:
				return "decrement";
			case CONTAINS:
				return "contains";
			case EQUALS:
				return "equals_";
			case COMPARE:
				return "compareTo";
			case RANGE:
				return "until";
			case CAST:
				return "cast";
			case CALL:
				return "call";
			case MEMBERGETTER:
				return "getMember";
			case MEMBERSETTER:
				return "setMember";
			default:
				throw new IllegalArgumentException("Invalid operator: " + operator);
		}
	}
}
