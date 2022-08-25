/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared.prepare;

import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.javashared.*;

/**
 * @author Hoofdgebruiker
 */
public class JavaPrepareClassMethodVisitor implements MemberVisitor<Void> {
	private static final boolean DEBUG_EMPTY = true;

	private final JavaContext context;
	private final JavaCompiledModule module;
	private final JavaClass cls;
	private final JavaNativeClass nativeClass;
	private final JavaPrepareDefinitionMemberVisitor memberPreparer;

	public JavaPrepareClassMethodVisitor(
			JavaContext context,
			JavaCompiledModule module,
			JavaClass cls,
			JavaNativeClass nativeClass,
			JavaPrepareDefinitionMemberVisitor memberPreparer,
			boolean startsEmpty) {
		this.context = context;
		this.module = module;
		this.cls = cls;
		this.nativeClass = nativeClass;
		this.memberPreparer = memberPreparer;

		cls.empty = startsEmpty;
	}

	@Override
	public Void visitField(FieldMember member) {
		JavaField field = new JavaField(cls, member.name, context.getDescriptor(member.getType()), context.getSignature(member.getType()));
		module.setFieldInfo(member, field);
		if (member.hasAutoGetter()) {
			visitGetter(member.autoGetter);
			module.setFieldInfo(member.autoGetter, field);
		}
		if (member.hasAutoSetter()) {
			visitSetter(member.autoSetter);
			module.setFieldInfo(member.autoSetter, field);
		}

		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		visitFunctional(member, member.header, "<init>");
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
	public Void visitImplementation(ImplementationMember member) {
		memberPreparer.prepare(member.type);

		if (canMergeImplementation(member)) {
			module.setImplementationInfo(member, new JavaImplementation(true, cls));
			for (IDefinitionMember m : member.members)
				m.accept(this);
		} else {
			if (DEBUG_EMPTY && cls.empty)
				context.logger.trace("Class " + cls.fullName + " not empty because of unmergeable implementation");

			cls.empty = false;

			JavaClass implementationClass = new JavaClass(cls, JavaTypeNameVisitor.INSTANCE.process(member.type) + "Implementation", JavaClass.Kind.CLASS);
			module.setImplementationInfo(member, new JavaImplementation(false, implementationClass));

			JavaPrepareClassMethodVisitor visitor = new JavaPrepareClassMethodVisitor(context, module, implementationClass, null, memberPreparer, true);
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
		JavaPrepareDefinitionMemberVisitor innerDefinitionPrepare = new JavaPrepareDefinitionMemberVisitor(context, module);
		member.innerDefinition.accept(innerDefinitionPrepare);

		if (DEBUG_EMPTY && cls.empty)
			context.logger.trace("Class " + cls.fullName + " not empty because of inner definition " + member.innerDefinition.name);
		cls.empty = false;
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		if (DEBUG_EMPTY && cls.empty)
			context.logger.trace("Class " + cls.fullName + " not empty because of static initializer");

		cls.empty = false;
		return null;
	}

	private JavaNativeMethod.Kind getKind(DefinitionMember member) {
		if (member instanceof ConstructorMember)
			return JavaNativeMethod.Kind.CONSTRUCTOR;

		return member.isStatic() ? JavaNativeMethod.Kind.STATIC : JavaNativeMethod.Kind.INSTANCE;
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
			case DESTRUCTOR:
				return "close";
			default:
				throw new IllegalArgumentException("Invalid operator: " + operator);
		}
	}

	private void visitFunctional(DefinitionMember member, FunctionHeader header, String name) {
		NativeTag nativeTag = member.getTag(NativeTag.class);
		JavaNativeMethod method = null;
		if (nativeTag != null && nativeClass != null)
			method = nativeClass.getMethod(nativeTag.value);

		int modifiers = cls.kind == JavaClass.Kind.INTERFACE ? JavaModifiers.ABSTRACT : 0;
		if (member.getOverrides() != null) {
			MethodSymbol base = member.getOverrides();

			JavaNativeMethod baseMethod = context.getJavaMethod(base);

			method = new JavaNativeMethod(
					cls,
					baseMethod.kind,
					baseMethod.name,
					true,
					context.getMethodDescriptor(header),
					modifiers | JavaModifiers.getJavaModifiers(member.getEffectiveModifiers()),
					header.getReturnType() instanceof GenericTypeID,
					header.useTypeParameters());
		} else if (method == null) {
			if (member instanceof ConstructorMember) {
				method = new JavaNativeMethod(
						cls,
						getKind(member),
						name,
						true,
						context.getMethodDescriptorConstructor(header, member),
						modifiers | JavaModifiers.getJavaModifiers(member.getEffectiveModifiers()),
						false,
						header.useTypeParameters()
				);
			} else {
				method = new JavaNativeMethod(
						cls,
						getKind(member),
						name,
						true,
						context.getMethodDescriptor(header),
						modifiers | JavaModifiers.getJavaModifiers(member.getEffectiveModifiers()),
						header.getReturnType() instanceof GenericTypeID,
						header.useTypeParameters());
			}
		}

		if (method.compile && member.getBuiltin() != BuiltinID.CLASS_DEFAULT_CONSTRUCTOR) {
			if (DEBUG_EMPTY && cls.empty)
				context.logger.trace("Class " + cls.fullName + " not empty because of " + member.describe());

			cls.empty = false;
		}

		module.setMethodInfo(member, method);
	}
}
