/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared.prepare;

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
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.javashared.JavaTypeNameVisitor;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaImplementation;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaModifiers;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaPrepareClassMethodVisitor implements MemberVisitor<Void> {
	private static final boolean DEBUG_EMPTY = true;
	
	private final JavaContext context;
	private final JavaClass cls;
	private final JavaNativeClass nativeClass;
	private final JavaPrepareDefinitionMemberVisitor memberPreparer;
	
	public JavaPrepareClassMethodVisitor(
			JavaContext context,
			JavaClass cls,
			JavaNativeClass nativeClass,
			JavaPrepareDefinitionMemberVisitor memberPreparer,
			boolean startsEmpty) {
		this.context = context;
		this.cls = cls;
		this.nativeClass = nativeClass;
		this.memberPreparer = memberPreparer;
		
		cls.empty = startsEmpty;
	}
	
	@Override
	public Void visitConst(ConstMember member) {
		if (DEBUG_EMPTY && cls.empty)
			System.out.println("Class " + cls.fullName + " not empty because of const " + member.name);
		
		cls.empty = false;
		member.setTag(JavaField.class, new JavaField(cls, member.name, context.getDescriptor(member.type)));
		return null;
	}
	
	@Override
	public Void visitField(FieldMember member) {
		JavaField field = new JavaField(cls, member.name, context.getDescriptor(member.type));
		member.setTag(JavaField.class, field);
		if (member.hasAutoGetter()) {
			visitGetter(member.autoGetter);
			member.autoGetter.setTag(JavaField.class, field);
		}
		if (member.hasAutoSetter()) {
			visitSetter(member.autoSetter);
		}
		
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		visitFunctional(member, member.header, "<init>");
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		if (DEBUG_EMPTY && cls.empty)
			System.out.println("Class " + cls.fullName + " not empty because of destructor");
		
		cls.empty = false;
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		visitFunctional(member, member.header, member.name);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		visitFunctional(member, new FunctionHeader(member.type), "get" + StringExpansion.capitalize(member.name));
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		visitFunctional(member, new FunctionHeader(BasicTypeID.VOID, member.type), "set" + StringExpansion.capitalize(member.name));
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
		memberPreparer.prepare(member.type);
		
		if (canMergeImplementation(member)) {
			member.setTag(JavaImplementation.class, new JavaImplementation(true, cls));
			for (IDefinitionMember m : member.members)
				m.accept(this);
		} else {
			if (DEBUG_EMPTY && cls.empty)
				System.out.println("Class " + cls.fullName + " not empty because of unmergeable implementation");
			
			cls.empty = false;
			
			JavaClass implementationClass = new JavaClass(cls, JavaTypeNameVisitor.INSTANCE.process(member.type) + "Implementation", JavaClass.Kind.CLASS);
			member.setTag(JavaImplementation.class, new JavaImplementation(false, implementationClass));
			
			JavaPrepareClassMethodVisitor visitor = new JavaPrepareClassMethodVisitor(context, implementationClass, null, memberPreparer, true);
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
		JavaPrepareDefinitionMemberVisitor innerDefinitionPrepare = new JavaPrepareDefinitionMemberVisitor(context);
		member.innerDefinition.accept(innerDefinitionPrepare);
		
		if (DEBUG_EMPTY && cls.empty)
			System.out.println("Class " + cls.fullName + " not empty because of inner definition " + member.innerDefinition.name);
		cls.empty = false;
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		if (DEBUG_EMPTY && cls.empty)
			System.out.println("Class " + cls.fullName + " not empty because of static initializer");
		
		cls.empty = false;
		return null;
	}
	
	private JavaMethod.Kind getKind(DefinitionMember member) {
		if (member instanceof ConstructorMember)
			return JavaMethod.Kind.CONSTRUCTOR;
		
		return member.isStatic() ? JavaMethod.Kind.STATIC : JavaMethod.Kind.INSTANCE;
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
	
	private void visitFunctional(DefinitionMember member, FunctionHeader header, String name) {
		NativeTag nativeTag = member.getTag(NativeTag.class);
		JavaMethod method = null;
		if (nativeTag != null && nativeClass != null)
			method = nativeClass.getMethod(nativeTag.value);
		
		if (member.getOverrides() != null) {
			DefinitionMemberRef base = member.getOverrides();
			JavaMethod baseMethod = base.getTarget().getTag(JavaMethod.class);
			if (baseMethod == null)
				throw new IllegalStateException("Base method not yet prepared!");
			
			method = new JavaMethod(
					cls,
					baseMethod.kind,
					baseMethod.name,
					true,
					context.getMethodDescriptor(header),
					JavaModifiers.getJavaModifiers(member.getEffectiveModifiers()),
					header.getReturnType().type instanceof GenericTypeID);
		} else if (method == null) {
			method = new JavaMethod(
					cls,
					getKind(member),
					name,
					true,
					context.getMethodDescriptor(header),
					JavaModifiers.getJavaModifiers(member.getEffectiveModifiers()),
					header.getReturnType().type instanceof GenericTypeID);
		}
		
		if (method.compile && member.getBuiltin() != BuiltinID.CLASS_DEFAULT_CONSTRUCTOR) {
			if (DEBUG_EMPTY && cls.empty)
				System.out.println("Class " + cls.fullName + " not empty because of " + member.describe());
			
			cls.empty = false;
		}
		
		member.setTag(JavaMethod.class, method);
	}
}
