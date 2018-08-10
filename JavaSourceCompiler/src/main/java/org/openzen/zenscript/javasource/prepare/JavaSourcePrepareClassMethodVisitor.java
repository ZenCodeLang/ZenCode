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
public class JavaSourcePrepareClassMethodVisitor implements MemberVisitor<Void> {
	private final String filename;
	private final JavaSourceClass cls;
	private final JavaNativeClass nativeClass;
	
	public JavaSourcePrepareClassMethodVisitor(String filename, JavaSourceClass cls, JavaNativeClass nativeClass, boolean startsEmpty) {
		this.filename = filename;
		this.cls = cls;
		this.nativeClass = nativeClass;
		cls.empty = startsEmpty;
	}
	
	@Override
	public Void visitConst(ConstMember member) {
		cls.empty = false;
		member.setTag(JavaSourceField.class, new JavaSourceField(cls, member.name));
		return null;
	}
	
	@Override
	public Void visitField(FieldMember member) {
		cls.empty = false;
		member.setTag(JavaSourceField.class, new JavaSourceField(cls, member.name));
		if (member.hasAutoGetter())
			visitGetter(member.autoGetter);
		if (member.hasAutoSetter())
			visitSetter(member.autoSetter);
		
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		visitFunctional(member, "<init>");
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		cls.empty = false;
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
		cls.empty = false;
		if (canMergeImplementation(member)) {
			member.setTag(JavaSourceImplementation.class, new JavaSourceImplementation(true, cls));
			for (IDefinitionMember m : member.members)
				m.accept(this);
		} else {
			JavaSourceClass implementationClass = new JavaSourceClass(cls, member.type.accept(new JavaSourceTypeNameVisitor()) + "Implementation");
			member.setTag(JavaSourceImplementation.class, new JavaSourceImplementation(false, implementationClass));
			
			JavaSourcePrepareClassMethodVisitor visitor = new JavaSourcePrepareClassMethodVisitor(filename, implementationClass, null, true);
			for (IDefinitionMember m : member.members)
				m.accept(visitor);
		}
		return null;
	}
	
	private boolean canMergeImplementation(ImplementationMember member) {
		return true; // TODO: implementation merge check
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		JavaSourcePrepareDefinitionVisitor innerDefinitionPrepare = new JavaSourcePrepareDefinitionVisitor(filename, cls);
		member.innerDefinition.accept(innerDefinitionPrepare);
		
		cls.empty = false;
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		cls.empty = false;
		return null;
	}
	
	private JavaSourceMethod.Kind getKind(DefinitionMember member) {
		if (member instanceof ConstructorMember)
			return JavaSourceMethod.Kind.CONSTRUCTOR;
		
		return member.isStatic() ? JavaSourceMethod.Kind.STATIC : JavaSourceMethod.Kind.INSTANCE;
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
	
	private void visitFunctional(DefinitionMember member, String name) {
		NativeTag nativeTag = member.getTag(NativeTag.class);
		JavaSourceMethod method = null;
		if (nativeTag != null && nativeClass != null)
			method = nativeClass.getMethod(nativeTag.value);
		if (method == null)
			method = new JavaSourceMethod(cls, getKind(member), name, true);
		
		if (method.compile)
			cls.empty = false;
		
		member.setTag(JavaSourceMethod.class, method);
	}
}
