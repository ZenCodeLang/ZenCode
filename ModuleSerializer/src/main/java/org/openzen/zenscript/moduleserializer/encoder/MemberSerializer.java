/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.member.MemberVisitorWithContext;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.moduleserialization.MemberEncoding;
import org.openzen.zenscript.moduleserializer.SerializationOptions;

/**
 * @author Hoofdgebruiker
 */
public class MemberSerializer implements MemberVisitorWithContext<TypeContext, Void> {
	private final CodeSerializationOutput output;
	private final SerializationOptions options;

	public MemberSerializer(CodeSerializationOutput output, SerializationOptions options) {
		this.output = output;
		this.options = options;
	}

	private int getFlags(IDefinitionMember member) {
		int flags = 0;
		if (member.getPosition() != CodePosition.UNKNOWN && options.positions)
			flags |= MemberEncoding.FLAG_POSITION;
		if (member.getAnnotations().length > 0)
			flags |= MemberEncoding.FLAG_ANNOTATIONS;

		return flags;
	}

	private void serialize(int flags, TypeContext context, IDefinitionMember member) {
		output.writeUInt(flags);
		if ((flags & MemberEncoding.FLAG_POSITION) > 0)
			output.serialize(member.getPosition());

		output.writeUInt(member.getSpecifiedModifiers());

		if (member.getAnnotations().length > 0) {
			output.enqueueCode(output -> {
				output.writeUInt(member.getAnnotations().length);
				for (MemberAnnotation annotation : member.getAnnotations()) {
					output.write(annotation.getDefinition());
					annotation.serialize(output, member, context);
				}
			});
		}
	}

	private void writeName(int flags, String name) {
		if ((flags & MemberEncoding.FLAG_NAME) > 0)
			output.writeString(name);
	}

	@Override
	public Void visitConst(TypeContext context, ConstMember member) {
		output.writeUInt(MemberEncoding.TYPE_CONST);

		int flags = getFlags(member);
		if (member.name != null)
			flags |= MemberEncoding.FLAG_NAME;

		serialize(flags, context, member);
		if ((flags & MemberEncoding.FLAG_NAME) > 0)
			output.writeString(member.name);

		output.serialize(context, member.type);
		output.enqueueCode(encoder -> encoder.serialize(new StatementContext(context), member.value));
		return null;
	}

	@Override
	public Void visitField(TypeContext context, FieldMember member) {
		output.writeUInt(MemberEncoding.TYPE_FIELD);

		int flags = getFlags(member);
		if (member.name != null)
			flags |= MemberEncoding.FLAG_NAME;
		if (member.autoGetterAccess != 0)
			flags |= MemberEncoding.FLAG_AUTO_GETTER;
		if (member.autoSetterAccess != 0)
			flags |= MemberEncoding.FLAG_AUTO_SETTER;

		serialize(flags, context, member);
		writeName(flags, member.name);
		if ((flags & MemberEncoding.FLAG_AUTO_GETTER) > 0)
			output.writeUInt(member.autoGetterAccess);
		if ((flags & MemberEncoding.FLAG_AUTO_SETTER) > 0)
			output.writeUInt(member.autoSetterAccess);

		output.serialize(context, member.type);
		output.enqueueCode(encoder -> encoder.serialize(new StatementContext(context), member.initializer));
		return null;
	}

	@Override
	public Void visitConstructor(TypeContext context, ConstructorMember member) {
		output.writeUInt(MemberEncoding.TYPE_CONSTRUCTOR);

		int flags = getFlags(member);
		serialize(flags, context, member);

		StatementContext inner = new StatementContext(context, member.header);
		output.serialize(inner, member.header);

		output.enqueueCode(encoder -> {
			encoder.serialize(inner, member.body);
		});
		return null;
	}

	@Override
	public Void visitDestructor(TypeContext context, DestructorMember member) {
		output.writeUInt(MemberEncoding.TYPE_DESTRUCTOR);
		int flags = getFlags(member);
		serialize(flags, context, member);

		output.enqueueCode(encoder -> {
			encoder.write(context, member.overrides);
			encoder.serialize(new StatementContext(context, member.header), member.body);
		});
		return null;
	}

	@Override
	public Void visitMethod(TypeContext context, MethodMember member) {
		output.writeUInt(MemberEncoding.TYPE_METHOD);
		int flags = getFlags(member);
		if (member.name != null)
			flags |= MemberEncoding.FLAG_NAME;
		serialize(flags, context, member);
		writeName(flags, member.name);

		StatementContext inner = new StatementContext(context, member.header);
		output.serialize(inner, member.header);

		output.enqueueCode(encoder -> {
			encoder.write(inner, member.getOverrides());
			encoder.serialize(inner, member.body);
		});
		return null;
	}

	@Override
	public Void visitGetter(TypeContext context, GetterMember member) {
		output.writeUInt(MemberEncoding.TYPE_GETTER);
		int flags = getFlags(member);
		if (member.name != null)
			flags |= MemberEncoding.FLAG_NAME;
		serialize(flags, context, member);
		output.serialize(context, member.type);
		writeName(flags, member.name);

		output.enqueueCode(encoder -> {
			encoder.write(context, member.getOverrides());
			encoder.serialize(new StatementContext(context, new FunctionHeader(member.type)), member.body);
		});
		return null;
	}

	@Override
	public Void visitSetter(TypeContext context, SetterMember member) {
		output.writeUInt(MemberEncoding.TYPE_SETTER);
		int flags = getFlags(member);
		serialize(flags, context, member);
		output.serialize(context, member.type);
		writeName(flags, member.name);

		output.enqueueCode(encoder -> {
			encoder.write(context, member.getOverrides());
			encoder.serialize(new StatementContext(
							context,
							new FunctionHeader(BasicTypeID.VOID, member.parameter)),
					member.body);
		});
		return null;
	}

	@Override
	public Void visitOperator(TypeContext context, OperatorMember member) {
		output.writeUInt(MemberEncoding.TYPE_OPERATOR);
		int flags = getFlags(member);
		serialize(flags, context, member);
		output.writeUInt(getId(member.operator));

		StatementContext inner = new StatementContext(context, member.header);
		output.serialize(inner, member.header);

		output.enqueueCode(encoder -> {
			encoder.write(inner, member.getOverrides());
			encoder.serialize(inner, member.body);
		});
		return null;
	}

	@Override
	public Void visitCaster(TypeContext context, CasterMember member) {
		output.writeUInt(MemberEncoding.TYPE_CASTER);
		int flags = getFlags(member);
		serialize(flags, context, member);
		output.serialize(context, member.toType);

		output.enqueueCode(encoder -> {
			encoder.write(context, member.getOverrides());
			encoder.serialize(
					new StatementContext(context, new FunctionHeader(member.toType)),
					member.body);
		});
		return null;
	}

	@Override
	public Void visitIterator(TypeContext context, IteratorMember member) {
		output.writeUInt(MemberEncoding.TYPE_ITERATOR);
		int flags = getFlags(member);
		serialize(flags, context, member);
		output.writeUInt(member.getLoopVariableCount());
		for (TypeID type : member.getLoopVariableTypes())
			output.serialize(context, type);

		output.enqueueCode(encoder -> {
			encoder.write(context, member.getOverrides());
			encoder.serialize(
					new StatementContext(context, member.header),
					member.body);
		});
		return null;
	}

	@Override
	public Void visitCaller(TypeContext context, CallerMember member) {
		output.writeUInt(MemberEncoding.TYPE_CALLER);
		int flags = getFlags(member);
		serialize(flags, context, member);

		StatementContext inner = new StatementContext(context, member.header);
		output.serialize(inner, member.header);

		output.enqueueCode(encoder -> {
			encoder.write(inner, member.getOverrides());
			encoder.serialize(inner, member.body);
		});
		return null;
	}

	@Override
	public Void visitImplementation(TypeContext context, ImplementationMember member) {
		output.writeUInt(MemberEncoding.TYPE_IMPLEMENTATION);
		int flags = getFlags(member);
		serialize(flags, context, member);
		output.serialize(context, member.type);

		output.writeUInt(member.members.size());
		for (IDefinitionMember implementationMember : member.members) {
			implementationMember.accept(context, this);
		}
		return null;
	}

	@Override
	public Void visitInnerDefinition(TypeContext context, InnerDefinitionMember member) {
		// already serialized
		return null;
	}

	@Override
	public Void visitStaticInitializer(TypeContext context, StaticInitializerMember member) {
		output.writeUInt(MemberEncoding.TYPE_STATIC_INITIALIZER);
		int flags = getFlags(member);
		serialize(flags, context, member);

		output.enqueueCode(encoder -> {
			encoder.serialize(new StatementContext(context, new FunctionHeader(BasicTypeID.VOID)), member.body);
		});
		return null;
	}

	private int getId(OperatorType operator) {
		switch (operator) {
			case ADD:
				return MemberEncoding.OPERATOR_ADD;
			case SUB:
				return MemberEncoding.OPERATOR_SUB;
			case MUL:
				return MemberEncoding.OPERATOR_MUL;
			case DIV:
				return MemberEncoding.OPERATOR_DIV;
			case MOD:
				return MemberEncoding.OPERATOR_MOD;
			case CAT:
				return MemberEncoding.OPERATOR_CAT;
			case OR:
				return MemberEncoding.OPERATOR_OR;
			case AND:
				return MemberEncoding.OPERATOR_AND;
			case XOR:
				return MemberEncoding.OPERATOR_XOR;
			case NEG:
				return MemberEncoding.OPERATOR_NEG;
			case INVERT:
				return MemberEncoding.OPERATOR_INVERT;
			case NOT:
				return MemberEncoding.OPERATOR_NOT;
			case INDEXSET:
				return MemberEncoding.OPERATOR_INDEXSET;
			case INDEXGET:
				return MemberEncoding.OPERATOR_INDEXGET;
			case CONTAINS:
				return MemberEncoding.OPERATOR_CONTAINS;
			case COMPARE:
				return MemberEncoding.OPERATOR_COMPARE;
			case MEMBERGETTER:
				return MemberEncoding.OPERATOR_MEMBERGETTER;
			case MEMBERSETTER:
				return MemberEncoding.OPERATOR_MEMBERSETTER;
			case EQUALS:
				return MemberEncoding.OPERATOR_EQUALS;
			case NOTEQUALS:
				return MemberEncoding.OPERATOR_NOTEQUALS;
			case SAME:
				return MemberEncoding.OPERATOR_SAME;
			case NOTSAME:
				return MemberEncoding.OPERATOR_NOTSAME;
			case SHL:
				return MemberEncoding.OPERATOR_SHL;
			case SHR:
				return MemberEncoding.OPERATOR_SHR;
			case USHR:
				return MemberEncoding.OPERATOR_USHR;
			case ADDASSIGN:
				return MemberEncoding.OPERATOR_ADDASSIGN;
			case SUBASSIGN:
				return MemberEncoding.OPERATOR_SUBASSIGN;
			case MULASSIGN:
				return MemberEncoding.OPERATOR_MULASSIGN;
			case DIVASSIGN:
				return MemberEncoding.OPERATOR_DIVASSIGN;
			case MODASSIGN:
				return MemberEncoding.OPERATOR_MODASSIGN;
			case CATASSIGN:
				return MemberEncoding.OPERATOR_CATASSIGN;
			case ORASSIGN:
				return MemberEncoding.OPERATOR_ORASSIGN;
			case XORASSIGN:
				return MemberEncoding.OPERATOR_XORASSIGN;
			case SHLASSIGN:
				return MemberEncoding.OPERATOR_SHLASSIGN;
			case SHRASSIGN:
				return MemberEncoding.OPERATOR_SHRASSIGN;
			case USHRASSIGN:
				return MemberEncoding.OPERATOR_USHRASSIGN;
			case INCREMENT:
				return MemberEncoding.OPERATOR_INCREMENT;
			case DECREMENT:
				return MemberEncoding.OPERATOR_DECREMENT;
			case RANGE:
				return MemberEncoding.OPERATOR_RANGE;
			default:
				throw new IllegalArgumentException("Unknown operator: " + operator);
		}
	}
}
