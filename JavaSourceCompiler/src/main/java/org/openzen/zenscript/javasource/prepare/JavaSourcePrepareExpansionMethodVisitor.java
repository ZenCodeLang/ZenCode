/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.prepare;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.CustomIteratorMember;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
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
import org.openzen.zenscript.javasource.tags.JavaSourceMethod;
import org.openzen.zenscript.shared.StringUtils;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourcePrepareExpansionMethodVisitor implements MemberVisitor<Void> {
	private final JavaSourceClass cls;
	
	public JavaSourcePrepareExpansionMethodVisitor(JavaSourceClass cls) {
		this.cls = cls;
	}
	
	@Override
	public Void visitField(FieldMember member) {
		// TODO: expansion fields
		member.setTag(JavaSourceField.class, new JavaSourceField(cls, member.name));
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		member.setTag(
				JavaSourceMethod.class,
				new JavaSourceMethod(cls, getKind(member), member.name));
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		member.setTag(
				JavaSourceMethod.class,
				new JavaSourceMethod(cls, getKind(member), "get" + StringUtils.capitalize(member.name)));
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		member.setTag(
				JavaSourceMethod.class,
				new JavaSourceMethod(cls, getKind(member), "set" + StringUtils.capitalize(member.name)));
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		member.setTag(
				JavaSourceMethod.class,
				new JavaSourceMethod(cls, getKind(member), getOperatorName(member.operator)));
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		member.setTag(JavaSourceMethod.class, new JavaSourceMethod(
				cls,
				JavaSourceMethod.Kind.INSTANCE,
				"to" + member.toType.accept(new JavaSourceTypeNameVisitor())));
		return null;
	}

	@Override
	public Void visitCustomIterator(CustomIteratorMember member) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitCaller(CallerMember member) {
		member.setTag(JavaSourceMethod.class, new JavaSourceMethod(cls, getKind(member), "call"));
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		// TODO: implementation merge check
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		// TODO
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		return null;
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
				return "equals";
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