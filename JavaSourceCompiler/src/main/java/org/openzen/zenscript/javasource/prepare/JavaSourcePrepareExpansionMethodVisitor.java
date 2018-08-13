/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.prepare;

import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.CustomIteratorMember;
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
import org.openzen.zenscript.javasource.JavaSourceTypeNameVisitor;
import org.openzen.zenscript.javasource.tags.JavaSourceClass;
import org.openzen.zenscript.javasource.tags.JavaSourceField;
import org.openzen.zenscript.javasource.tags.JavaSourceImplementation;
import org.openzen.zenscript.javasource.tags.JavaSourceMethod;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourcePrepareExpansionMethodVisitor implements MemberVisitor<Void> {
	private static final boolean DEBUG_EMPTY = true;
	
	private final JavaSourceClass cls;
	private final JavaNativeClass nativeClass;
	
	public JavaSourcePrepareExpansionMethodVisitor(JavaSourceClass cls, JavaNativeClass nativeClass) {
		this.cls = cls;
		this.nativeClass = nativeClass;
		cls.empty = true;
	}
	
	@Override
	public Void visitConst(ConstMember member) {
		member.setTag(JavaSourceField.class, new JavaSourceField(cls, member.name));
		
		if (DEBUG_EMPTY && cls.empty)
			System.out.println("Class " + cls.fullName + " not empty because of const");
		
		cls.empty = false;
		return null;
	}
	
	@Override
	public Void visitField(FieldMember member) {
		// TODO: expansion fields
		member.setTag(JavaSourceField.class, new JavaSourceField(cls, member.name));
		if (member.hasAutoGetter() || member.hasAutoSetter())
			cls.empty = false;
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		visitFunctional(member, "");
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		if (nativeClass != null && nativeClass.nonDestructible)
			return null;
		
		visitFunctional(member, "");
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		visitFunctional(member, member.name);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		visitFunctional(member, "get" + StringExpansion.capitalize(member.name));
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		visitFunctional(member, "set" + StringExpansion.capitalize(member.name));
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		visitFunctional(member, getOperatorName(member.operator));
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		visitFunctional(member, "to" + member.toType.accept(new JavaSourceTypeNameVisitor()));
		return null;
	}

	@Override
	public Void visitCustomIterator(CustomIteratorMember member) {
		visitFunctional(member, member.getLoopVariableCount() == 1 ? "iterator" : "iterator" + member.getLoopVariableCount());
		return null;
	}

	@Override
	public Void visitCaller(CallerMember member) {
		visitFunctional(member, "call");
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		JavaSourceClass implementationClass = new JavaSourceClass(cls, member.type.accept(new JavaSourceTypeNameVisitor()) + "Implementation");
		member.setTag(JavaSourceImplementation.class, new JavaSourceImplementation(false, implementationClass));
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
	
	private void visitFunctional(DefinitionMember member, String name) {
		NativeTag nativeTag = member.getTag(NativeTag.class);
		JavaSourceMethod method = null;
		if (nativeTag != null && nativeClass != null)
			method = nativeClass.getMethod(nativeTag.value);
		if (method == null)
			method = new JavaSourceMethod(cls, getKind(member), name, true); 
		
		if (method.compile) {
			if (DEBUG_EMPTY && cls.empty)
				System.out.println("Class " + cls.fullName + " not empty because of " + member.describe());
			
			cls.empty = false;
		}
		
		member.setTag(JavaSourceMethod.class, method);
	}
	
	private JavaSourceMethod.Kind getKind(DefinitionMember member) {
		return member.isStatic() ? JavaSourceMethod.Kind.STATIC : JavaSourceMethod.Kind.EXPANSION;
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
