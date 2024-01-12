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
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;
import org.openzen.zenscript.javashared.compiling.JavaCompilingModule;

/**
 * @author Hoofdgebruiker
 */
public class JavaPrepareClassMethodVisitor implements MemberVisitor<Void> {
	private static final boolean DEBUG_EMPTY = true;

	private final JavaContext context;
	private final JavaCompilingModule module;
	private final JavaCompilingClass class_;
	private final JavaPrepareDefinitionMemberVisitor memberPreparer;

	public JavaPrepareClassMethodVisitor(
			JavaCompilingClass class_,
			JavaPrepareDefinitionMemberVisitor memberPreparer,
			boolean startsEmpty) {
		this.context = class_.getContext();
		this.module = class_.module;
		this.class_ = class_;
		this.memberPreparer = memberPreparer;

		class_.empty = startsEmpty;
	}

	@Override
	public Void visitField(FieldMember member) {
		JavaNativeField field = new JavaNativeField(class_.compiled, member.name, context.getDescriptor(member.getType()), context.getSignature(member.getType()));
		class_.addField(member, field);
		if (member.hasAutoGetter()) {
			visitGetter(member.autoGetter);
			class_.module.module.setFieldInfo(member.autoGetter, field);
		}
		if (member.hasAutoSetter()) {
			visitSetter(member.autoSetter);
			class_.module.module.setFieldInfo(member.autoSetter, field);
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
	public Void visitImplementation(ImplementationMember member) {
		if (canMergeImplementation(member)) {
			module.module.setImplementationInfo(member, new JavaImplementation(true, class_.compiled));
			for (IDefinitionMember m : member.members)
				m.accept(this);
		} else {
			if (DEBUG_EMPTY && class_.empty)
				context.logger.trace("Class " + class_.compiled.fullName + " not empty because of unmergeable implementation");

			class_.empty = false;

			JavaClass implementationClass = new JavaClass(class_.compiled, JavaTypeNameVisitor.INSTANCE.process(member.type) + "Implementation", JavaClass.Kind.CLASS);
			JavaCompilingClass implementationCompilingClass = new JavaCompilingClass(module, class_.symbol, implementationClass, null);
			module.module.setImplementationInfo(member, new JavaImplementation(false, implementationClass));

			JavaPrepareClassMethodVisitor visitor = new JavaPrepareClassMethodVisitor(implementationCompilingClass, memberPreparer, true);
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
		JavaPrepareDefinitionMemberVisitor innerDefinitionPrepare = new JavaPrepareDefinitionMemberVisitor(class_);
		member.innerDefinition.accept(innerDefinitionPrepare);

		if (DEBUG_EMPTY && class_.empty)
			context.logger.trace("Class " + class_.compiled.fullName + " not empty because of inner definition " + member.innerDefinition.name);
		class_.empty = false;
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		if (DEBUG_EMPTY && class_.empty)
			context.logger.trace("Class " + class_.compiled.fullName + " not empty because of static initializer");

		class_.empty = false;
		return null;
	}

	private JavaNativeMethod.Kind getKind(DefinitionMember member) {
		if (member instanceof ConstructorMember)
			return member.isImplicit() ? JavaNativeMethod.Kind.STATIC : JavaNativeMethod.Kind.CONSTRUCTOR;

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

	private void visitFunctional(FunctionalMember member, FunctionHeader header, String name) {
		NativeTag nativeTag = member.getTag(NativeTag.class);
		JavaCompilingMethod method = null;
		if (nativeTag != null && class_.nativeClass != null) {
			final String signature = context.getMethodSignature(header);
			method = new JavaCompilingMethod(class_.compiled, (JavaNativeMethod) class_.nativeClass.getMethod(nativeTag.value), signature);
		}

		int modifiers = class_.compiled.kind == JavaClass.Kind.INTERFACE ? JavaModifiers.ABSTRACT : 0;
		if (member.getOverrides().isPresent()) {
			MethodInstance base = member.getOverrides().get();
			JavaNativeMethod baseMethod = (JavaNativeMethod) context.getJavaMethod(base);

			// ToDo: Signature of base method vs. overridden method?
			final String signature = context.getMethodSignature(header);

			method = new JavaCompilingMethod(class_.compiled, new JavaNativeMethod(
					class_.compiled,
					baseMethod.kind,
					baseMethod.name,
					true,
					context.getMethodDescriptor(header),
					modifiers | JavaModifiers.getJavaModifiers(member.getEffectiveModifiers()),
					header.getReturnType() instanceof GenericTypeID,
					header.useTypeParameters()),
					signature);
		} else if (method == null) {
			if (member instanceof ConstructorMember) {
                if(member.isImplicit()) {
					final String signature = context.getMethodSignature(header, true);
					method = new JavaCompilingMethod(class_.compiled, new JavaNativeMethod(
							class_.compiled,
							getKind(member),
							"implicit-constructor",
							true,
							context.getMethodDescriptor(header),
							(JavaModifiers.getJavaModifiers(member.getEffectiveModifiers())),
							false,
							header.useTypeParameters()),
							signature);
				} else {
					final String signature = context.getMethodSignatureConstructor(member);
					method = new JavaCompilingMethod(class_.compiled, new JavaNativeMethod(
							class_.compiled,
							getKind(member),
                            name,
							true,
							context.getMethodDescriptorConstructor(member),
							// In Java, the .ctor is NOT static!
							(modifiers | JavaModifiers.getJavaModifiers(member.getEffectiveModifiers().withoutStatic())),
							false,
							header.useTypeParameters()),
							signature);
				}

			} else {
				final String signature = context.getMethodSignature(header);
				method = new JavaCompilingMethod(class_.compiled, new JavaNativeMethod(
						class_.compiled,
						getKind(member),
						name,
						true,
						context.getMethodDescriptor(header),
						modifiers | JavaModifiers.getJavaModifiers(member.getEffectiveModifiers()),
						header.getReturnType() instanceof GenericTypeID,
						header.useTypeParameters()),
						signature);
			}
		}

		if (DEBUG_EMPTY && class_.empty)
			context.logger.trace("Class " + class_.compiled.fullName + " not empty because of " + member.describe());

		class_.empty = false;

		class_.addMethod(member, method);
	}
}
