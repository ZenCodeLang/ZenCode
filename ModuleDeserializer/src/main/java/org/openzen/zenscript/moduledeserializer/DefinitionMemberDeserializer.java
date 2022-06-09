/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduledeserializer;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitorWithContext;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.member.ref.CasterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.member.ref.SetterMemberRef;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.moduleserialization.MemberEncoding;

/**
 * @author Hoofdgebruiker
 */
public class DefinitionMemberDeserializer implements DefinitionVisitorWithContext<TypeContext, Void> {
	private final CodeReader reader;

	public DefinitionMemberDeserializer(CodeReader reader) {
		this.reader = reader;
	}

	private void visit(TypeContext context, HighLevelDefinition definition) {
		TypeID superType = reader.deserializeType(context);
		definition.setSuperType(superType);

		IDefinitionMember[] members = new IDefinitionMember[reader.readUInt()];
		for (int i = 0; i < members.length; i++) {
			IDefinitionMember member = deserializeMember(context, definition, superType);
			if (member != null) {
				definition.members.add(member);
				reader.add(member);
			}
			members[i] = member;
		}
	}

	private CodePosition readPosition(int flags) {
		if ((flags & MemberEncoding.FLAG_POSITION) == 0)
			return CodePosition.UNKNOWN;

		return reader.deserializePosition();
	}

	private String readName(int flags) {
		if ((flags & MemberEncoding.FLAG_NAME) == 0)
			return null;

		return reader.readString();
	}

	private IDefinitionMember deserializeMember(TypeContext context, HighLevelDefinition definition, TypeID supertype) {
		int kind = reader.readUInt();
		int flags = 0;
		CodePosition position = CodePosition.UNKNOWN;
		int modifiers = 0;

		if (kind != MemberEncoding.TYPE_INNER_DEFINITION) {
			flags = reader.readUInt();
			position = readPosition(flags);
			modifiers = reader.readUInt();
		}

		DefinitionMember member;
		switch (kind) {
			case MemberEncoding.TYPE_CONST: {
				String name = readName(flags);
				TypeID type = reader.deserializeType(context);

				ConstMember result = new ConstMember(position, definition, modifiers, name, type, null);
				reader.enqueueCode(reader -> result.value = reader.deserializeExpression(new StatementContext(context)));
				member = result;
				break;
			}
			case MemberEncoding.TYPE_FIELD: {
				String name = readName(flags);
				int autoGetterAccess = (flags & MemberEncoding.FLAG_AUTO_GETTER) > 0 ? reader.readUInt() : 0;
				int autoSetterAccess = (flags & MemberEncoding.FLAG_AUTO_SETTER) > 0 ? reader.readUInt() : 0;
				TypeID type = reader.deserializeType(context);

				FieldMember result = new FieldMember(position, definition, flags, name, context.thisType, type, context.moduleContext.registry, autoGetterAccess, autoSetterAccess, null);
				reader.enqueueCode(reader -> result.initializer = reader.deserializeExpression(new StatementContext(context)));
				member = result;
				break;
			}
			case MemberEncoding.TYPE_CONSTRUCTOR: {
				FunctionHeader header = reader.deserializeHeader(context);
				ConstructorMember result = new ConstructorMember(position, definition, modifiers, header, null);
				reader.enqueueCode(reader -> {
					result.setBody(reader.deserializeStatement(new StatementContext(context, header)));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_DESTRUCTOR: {
				DestructorMember result = new DestructorMember(position, definition, modifiers);
				reader.enqueueCode(reader -> {
					result.overrides = (FunctionalMemberRef) reader.readMember(context, supertype);
					result.setBody(reader.deserializeStatement(new StatementContext(context, result.header)));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_METHOD: {
				String name = readName(flags);
				FunctionHeader header = reader.deserializeHeader(context);
				MethodMember result = new MethodMember(position, definition, modifiers, name, header, null);
				reader.enqueueCode(reader -> {
					result.setOverrides((FunctionalMemberRef) reader.readMember(context, supertype));
					result.setBody(reader.deserializeStatement(new StatementContext(context, header)));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_GETTER: {
				TypeID type = reader.deserializeType(context);
				String name = readName(flags);

				GetterMember result = new GetterMember(position, definition, modifiers, name, type, null);
				reader.enqueueCode(reader -> {
					result.setOverrides((GetterMemberRef) reader.readMember(context, supertype));
					FunctionHeader header = new FunctionHeader(type);
					result.setBody(reader.deserializeStatement(new StatementContext(context, header)));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_SETTER: {
				TypeID type = reader.deserializeType(context);
				String name = readName(flags);

				SetterMember result = new SetterMember(position, definition, modifiers, name, type, null);
				reader.enqueueCode(reader -> {
					result.setOverrides((SetterMemberRef) reader.readMember(context, supertype));
					FunctionHeader header = new FunctionHeader(BasicTypeID.VOID, result.parameter);
					result.setBody(reader.deserializeStatement(new StatementContext(context, header)));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_OPERATOR: {
				OperatorType operator = readOperator();
				FunctionHeader header = reader.deserializeHeader(context);

				OperatorMember result = new OperatorMember(position, definition, modifiers, operator, header, null);
				reader.enqueueCode(reader -> {
					result.setOverrides(context.moduleContext.registry, (FunctionalMemberRef) reader.readMember(context, supertype));
					result.setBody(reader.deserializeStatement(new StatementContext(context, header)));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_CASTER: {
				TypeID toType = reader.deserializeType(context);

				CasterMember result = new CasterMember(position, definition, modifiers, toType, null);
				reader.enqueueCode(reader -> {
					result.setOverrides(context.moduleContext.registry, (CasterMemberRef) reader.readMember(context, supertype));
					result.setBody(reader.deserializeStatement(new StatementContext(context, new FunctionHeader(toType))));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_ITERATOR: {
				TypeID[] types = new TypeID[reader.readUInt()];
				for (int i = 0; i < types.length; i++)
					types[i] = reader.deserializeType(context);

				IteratorMember result = new IteratorMember(position, definition, modifiers, types, context.moduleContext.registry, null);
				reader.enqueueCode(reader -> {
					result.setOverrides((IteratorMemberRef) reader.readMember(context, supertype));
					result.setBody(reader.deserializeStatement(new StatementContext(context, result.header)));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_CALLER: {
				FunctionHeader header = reader.deserializeHeader(context);
				CallerMember result = new CallerMember(position, definition, modifiers, header, null);

				reader.enqueueCode(reader -> {
					result.setOverrides(context.moduleContext.registry, (FunctionalMemberRef) reader.readMember(context, supertype));
					result.setBody(reader.deserializeStatement(new StatementContext(context, header)));
				});
				member = result;
				break;
			}
			case MemberEncoding.TYPE_IMPLEMENTATION: {
				TypeID type = reader.deserializeType(context);
				ImplementationMember result = new ImplementationMember(position, definition, modifiers, type);

				int members = reader.readUInt();
				for (int i = 0; i < members; i++) {
					IDefinitionMember imember = deserializeMember(context, definition, type);
					result.addMember(imember);
					reader.add(imember);
				}

				member = result;
				break;
			}
			case MemberEncoding.TYPE_INNER_DEFINITION:
				return null;
			case MemberEncoding.TYPE_STATIC_INITIALIZER: {
				StaticInitializerMember result = new StaticInitializerMember(position, definition);
				reader.enqueueCode(reader -> {
					result.body = reader.deserializeStatement(new StatementContext(context, new FunctionHeader(BasicTypeID.VOID)));
				});
				member = result;
				break;
			}
			default:
				throw new RuntimeException("Invalid member type: " + kind);
		}

		if ((flags & MemberEncoding.FLAG_ANNOTATIONS) > 0) {
			reader.enqueueCode(input -> {
				MemberAnnotation[] annotations = new MemberAnnotation[input.readUInt()];
				for (int i = 0; i < annotations.length; i++) {
					AnnotationDefinition type = input.readAnnotationType();
					annotations[i] = type.deserializeForMember(input, context, member);
				}
				member.annotations = annotations;
			});

		}
		return member;
	}

	private OperatorType readOperator() {
		int operator = reader.readUInt();
		switch (operator) {
			case MemberEncoding.OPERATOR_ADD:
				return OperatorType.ADD;
			case MemberEncoding.OPERATOR_SUB:
				return OperatorType.SUB;
			case MemberEncoding.OPERATOR_MUL:
				return OperatorType.MUL;
			case MemberEncoding.OPERATOR_DIV:
				return OperatorType.DIV;
			case MemberEncoding.OPERATOR_MOD:
				return OperatorType.MOD;
			case MemberEncoding.OPERATOR_CAT:
				return OperatorType.CAT;
			case MemberEncoding.OPERATOR_OR:
				return OperatorType.OR;
			case MemberEncoding.OPERATOR_XOR:
				return OperatorType.XOR;
			case MemberEncoding.OPERATOR_NEG:
				return OperatorType.NEG;
			case MemberEncoding.OPERATOR_INVERT:
				return OperatorType.INVERT;
			case MemberEncoding.OPERATOR_NOT:
				return OperatorType.NOT;
			case MemberEncoding.OPERATOR_INDEXSET:
				return OperatorType.INDEXSET;
			case MemberEncoding.OPERATOR_INDEXGET:
				return OperatorType.INDEXGET;
			case MemberEncoding.OPERATOR_CONTAINS:
				return OperatorType.CONTAINS;
			case MemberEncoding.OPERATOR_COMPARE:
				return OperatorType.COMPARE;
			case MemberEncoding.OPERATOR_MEMBERGETTER:
				return OperatorType.MEMBERGETTER;
			case MemberEncoding.OPERATOR_MEMBERSETTER:
				return OperatorType.MEMBERSETTER;
			case MemberEncoding.OPERATOR_EQUALS:
				return OperatorType.EQUALS;
			case MemberEncoding.OPERATOR_NOTEQUALS:
				return OperatorType.NOTEQUALS;
			case MemberEncoding.OPERATOR_SAME:
				return OperatorType.SAME;
			case MemberEncoding.OPERATOR_NOTSAME:
				return OperatorType.NOTSAME;
			case MemberEncoding.OPERATOR_SHL:
				return OperatorType.SHL;
			case MemberEncoding.OPERATOR_SHR:
				return OperatorType.SHR;
			case MemberEncoding.OPERATOR_USHR:
				return OperatorType.USHR;

			case MemberEncoding.OPERATOR_ADDASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_SUBASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_MULASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_DIVASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_MODASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_CATASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_ORASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_XORASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_SHLASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_SHRASSIGN:
				return OperatorType.ADDASSIGN;
			case MemberEncoding.OPERATOR_USHRASSIGN:
				return OperatorType.ADDASSIGN;

			case MemberEncoding.OPERATOR_INCREMENT:
				return OperatorType.INCREMENT;
			case MemberEncoding.OPERATOR_DECREMENT:
				return OperatorType.DECREMENT;

			case MemberEncoding.OPERATOR_RANGE:
				return OperatorType.RANGE;

			default:
				throw new RuntimeException("Invalid operator: " + operator);
		}
	}

	@Override
	public Void visitClass(TypeContext context, ClassDefinition definition) {
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitInterface(TypeContext context, InterfaceDefinition definition) {
		visit(context, definition);

		int baseInterfaces = reader.readUInt();
		for (int i = 0; i < baseInterfaces; i++)
			definition.baseInterfaces.add(reader.deserializeType(context));

		return null;
	}

	@Override
	public Void visitEnum(TypeContext context, EnumDefinition definition) {
		visit(context, definition);

		int constants = reader.readUInt();
		DefinitionTypeID type = context.moduleContext.registry.getForMyDefinition(definition);
		StatementContext initContext = new StatementContext(context);
		for (int i = 0; i < constants; i++) {
			int flags = reader.readUInt();
			String name = reader.readString();
			CodePosition position = readPosition(flags);

			EnumConstantMember constant = new EnumConstantMember(position, definition, name, i);
			reader.enqueueCode(input -> {
				FunctionalMemberRef constructor = (FunctionalMemberRef) reader.readMember(context, type);
				CallArguments arguments = reader.deserializeArguments(initContext);
				constant.constructor = new NewExpression(position, type, constructor, arguments);
			});
			definition.enumConstants.add(constant);
		}

		return null;
	}

	@Override
	public Void visitStruct(TypeContext context, StructDefinition definition) {
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitFunction(TypeContext context, FunctionDefinition definition) {
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitExpansion(TypeContext context, ExpansionDefinition definition) {
		visit(context, definition);
		definition.target = reader.deserializeType(context);
		return null;
	}

	@Override
	public Void visitAlias(TypeContext context, AliasDefinition definition) {
		definition.type = reader.deserializeType(context);
		return null;
	}

	@Override
	public Void visitVariant(TypeContext context, VariantDefinition variant) {
		visit(context, variant);

		int options = reader.readUInt();
		for (int i = 0; i < options; i++) {
			String name = reader.readString();
			TypeID[] types = new TypeID[reader.readUInt()];
			for (int j = 0; j < types.length; j++)
				types[i] = reader.deserializeType(context);

			variant.options.add(new VariantDefinition.Option(context.getPosition(), variant, name, i, types));
		}

		return null;
	}
}
