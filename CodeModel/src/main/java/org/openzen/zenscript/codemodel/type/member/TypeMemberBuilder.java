/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.expression.CallTranslator;
import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUIntExpression;
import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.EqualsMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.TranslatedOperatorMember;
import org.openzen.zenscript.codemodel.member.builtin.ArrayIteratorKeyValues;
import org.openzen.zenscript.codemodel.member.builtin.ArrayIteratorValues;
import org.openzen.zenscript.codemodel.member.builtin.ComparatorMember;
import org.openzen.zenscript.codemodel.member.builtin.ConstantGetterMember;
import org.openzen.zenscript.codemodel.member.builtin.RangeIterator;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;
import org.openzen.zenscript.codemodel.type.ConstTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import static org.openzen.zenscript.codemodel.type.member.BuiltinTypeMembers.*;
import org.openzen.zenscript.shared.CodePosition;
import static org.openzen.zenscript.shared.CodePosition.BUILTIN;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeMemberBuilder implements ITypeVisitor<Void> {
	private final TypeMembers members;
	private final LocalMemberCache cache;

	public TypeMemberBuilder(TypeMembers members, LocalMemberCache cache) {
		this.members = members;
		this.cache = cache;
		
		if (members.type != VOID) {
			members.addOperator(OperatorType.EQUALS, new EqualsMember(members.type), TypeMemberPriority.BUILTIN_DEFAULT);
		}
	}

	@Override
	public Void visitBasic(BasicTypeID basic) {
		switch (basic) {
			case BOOL:
				visitBool();
				break;
			case BYTE:
				visitByte();
				break;
			case INT:
				visitInt();
				break;
			case UINT:
				visitUInt();
				break;
			case LONG:
				visitLong();
				break;
			case CHAR:
				visitChar();
				break;
			case STRING:
				visitString();
				break;
		}
		return null;
	}

	@Override
	public Void visitArray(ArrayTypeID array) {
		ITypeID baseType = array.elementType;
		int dimension = array.dimension;

		FunctionParameter[] indexGetParameters = new FunctionParameter[dimension];
		for (int i = 0; i < dimension; i++)
			indexGetParameters[i] = new FunctionParameter(INT, null);

		FunctionHeader indexGetHeader = new FunctionHeader(baseType, indexGetParameters);
		members.addOperator(new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.INDEXGET, indexGetHeader), TypeMemberPriority.SPECIFIED);
		
		FunctionHeader sliceHeader = new FunctionHeader(array, new FunctionParameter(cache.getRegistry().getRange(BasicTypeID.INT, BasicTypeID.INT), "range"));
		members.addOperator(new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.INDEXGET, sliceHeader), TypeMemberPriority.SPECIFIED);
		
		FunctionHeader containsHeader = new FunctionHeader(BOOL, new FunctionParameter(baseType, "value"));
		members.addOperator(new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.CONTAINS, containsHeader), TypeMemberPriority.SPECIFIED);
		
		FunctionHeader sizedConstructorHeader = new FunctionHeader(VOID, indexGetParameters);
		members.addConstructor(new ConstructorMember(CodePosition.BUILTIN, 0, sizedConstructorHeader), TypeMemberPriority.SPECIFIED);

		FunctionParameter[] lambdaConstructorParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			lambdaConstructorParameters[i] = new FunctionParameter(INT, null);
		
		FunctionHeader lambdaConstructorFunction = new FunctionHeader(baseType, indexGetParameters);
		lambdaConstructorParameters[dimension] = new FunctionParameter(cache.getRegistry().getFunction(lambdaConstructorFunction), null);
		FunctionHeader lambdaConstructorHeader = new FunctionHeader(VOID, lambdaConstructorParameters);
		members.addConstructor(new ConstructorMember(CodePosition.BUILTIN, 0, lambdaConstructorHeader), TypeMemberPriority.SPECIFIED);
		
		FunctionParameter[] indexSetParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			indexSetParameters[i] = new FunctionParameter(INT, null);
		indexSetParameters[dimension] = new FunctionParameter(baseType, null);

		FunctionHeader indexSetHeader = new FunctionHeader(VOID, indexSetParameters);
		members.addOperator(new OperatorMember(CodePosition.BUILTIN, 0, OperatorType.INDEXSET, indexSetHeader), TypeMemberPriority.SPECIFIED);

		if (dimension == 1) {
			members.addConstructor(new ConstructorMember(CodePosition.BUILTIN, 0, new FunctionHeader(VOID)), TypeMemberPriority.SPECIFIED);
			
			FunctionHeader addHeader = new FunctionHeader(VOID, new FunctionParameter(baseType, "value"));
			members.addMethod(new MethodMember(CodePosition.BUILTIN, 0, "add", addHeader), TypeMemberPriority.SPECIFIED);

			members.addField(new FieldMember(CodePosition.BUILTIN, Modifiers.MODIFIER_FINAL, "length", INT), TypeMemberPriority.SPECIFIED);
		}

		members.addGetter(new GetterMember(CodePosition.BUILTIN, 0, "empty", BOOL), TypeMemberPriority.SPECIFIED);
		members.addIterator(new ArrayIteratorKeyValues(array), TypeMemberPriority.SPECIFIED);
		members.addIterator(new ArrayIteratorValues(array), TypeMemberPriority.SPECIFIED);
		members.addMethod(new MethodMember(CodePosition.BUILTIN, 0, "clear", new FunctionHeader(VOID)), TypeMemberPriority.SPECIFIED);
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		ITypeID keyType = assoc.keyType;
		ITypeID valueType = assoc.valueType;
		
		members.addConstructor(new ConstructorMember(BUILTIN, 0, new FunctionHeader(VOID)), TypeMemberPriority.SPECIFIED);

		FunctionHeader indexGetHeader = new FunctionHeader(valueType, new FunctionParameter(keyType, "key"));
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.INDEXGET, indexGetHeader), TypeMemberPriority.SPECIFIED);

		FunctionHeader indexSetHeader = new FunctionHeader(VOID, new FunctionParameter(keyType, "key"), new FunctionParameter(valueType, "value"));
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.INDEXSET, indexSetHeader), TypeMemberPriority.SPECIFIED);
		
		FunctionHeader getOrDefaultHeader = new FunctionHeader(valueType, new FunctionParameter(keyType, "key"), new FunctionParameter(valueType, "defaultValue"));
		members.addMethod(new MethodMember(BUILTIN, 0, "getOrDefault", getOrDefaultHeader), TypeMemberPriority.SPECIFIED);
		
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.CONTAINS, new FunctionHeader(BOOL, new FunctionParameter(keyType, "key"))), TypeMemberPriority.SPECIFIED);

		members.addField(new FieldMember(BUILTIN, Modifiers.MODIFIER_FINAL, "length", INT), TypeMemberPriority.SPECIFIED);
		members.addGetter(new GetterMember(BUILTIN, 0, "empty", BOOL), TypeMemberPriority.SPECIFIED);
		members.addGetter(new GetterMember(BUILTIN, 0, "keys", cache.getRegistry().getArray(keyType, 1)), TypeMemberPriority.SPECIFIED);
		return null;
	}
	
	@Override
	public Void visitIterator(IteratorTypeID iterator) {
		return null;
	}

	@Override
	public Void visitFunction(FunctionTypeID function) {
		members.addCaller(new CallerMember(BUILTIN, 0, function.header), TypeMemberPriority.SPECIFIED);
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID type) {
		HighLevelDefinition definition = type.definition;
		if (type.typeParameters.length > 0 || !type.outerTypeParameters.isEmpty()) {
			Map<TypeParameter, ITypeID> mapping = new HashMap<>();
			for (int i = 0; i < type.typeParameters.length; i++)
				mapping.put(definition.genericParameters.get(i), type.typeParameters[i]);
			for (Map.Entry<TypeParameter, ITypeID> entry : type.outerTypeParameters.entrySet())
				mapping.put(entry.getKey(), entry.getValue());
			
			for (IDefinitionMember member : definition.members) {
				member.instance(cache.getRegistry(), mapping).registerTo(members, TypeMemberPriority.SPECIFIED);
			}
		} else {
			for (IDefinitionMember member : definition.members) {
				member.registerTo(members, TypeMemberPriority.SPECIFIED);
			}
		}

		DefinitionMemberGroup constructors = members.getOrCreateGroup(OperatorType.CONSTRUCTOR);
		if (constructors.getMethodMembers().isEmpty()) {
			if (definition instanceof ClassDefinition) {
				// add default constructor
				constructors.addMethod(new ConstructorMember(BUILTIN, 0, new FunctionHeader(VOID)), TypeMemberPriority.SPECIFIED);
			} else if (definition instanceof StructDefinition) {
				// add default struct constructors
				constructors.addMethod(new ConstructorMember(BUILTIN, 0, new FunctionHeader(VOID)), TypeMemberPriority.SPECIFIED);
				
				List<FieldMember> fields = ((StructDefinition)definition).getFields();
				if (!fields.isEmpty()) {
					FunctionParameter[] parameters = new FunctionParameter[fields.size()];
					for (int i = 0; i < parameters.length; i++) {
						FieldMember field = fields.get(i);
						parameters[i] = new FunctionParameter(field.type, field.name, field.initializer, false);
					}
					constructors.addMethod(new ConstructorMember(BUILTIN, 0, new FunctionHeader(VOID, parameters)), TypeMemberPriority.SPECIFIED);
				}
			}
		}
		
		if (definition instanceof EnumDefinition) {
			members.addGetter(new GetterMember(BUILTIN, 0, "name", BasicTypeID.STRING), TypeMemberPriority.SPECIFIED);
			members.addGetter(new GetterMember(BUILTIN, 0, "ordinal", BasicTypeID.INT), TypeMemberPriority.SPECIFIED);
		}
		
		return null;
	}

	@Override
	public Void visitGeneric(GenericTypeID generic) {
		TypeParameter parameter = generic.parameter;

		for (GenericParameterBound bound : parameter.bounds) {
			bound.registerMembers(cache, members);
		}
		return null;
	}

	@Override
	public Void visitRange(RangeTypeID range) {
		ITypeID fromType = range.from;
		ITypeID toType = range.to;

		members.addField(new FieldMember(BUILTIN, Modifiers.MODIFIER_FINAL, "from", fromType), TypeMemberPriority.SPECIFIED);
		members.addField(new FieldMember(BUILTIN, Modifiers.MODIFIER_FINAL, "to", toType), TypeMemberPriority.SPECIFIED);
		members.addIterator(new RangeIterator(range), TypeMemberPriority.SPECIFIED);
		return null;
	}

	@Override
	public Void visitConst(ConstTypeID type) {
		return type.baseType.accept(this);
	}

	@Override
	public Void visitOptional(OptionalTypeID optional) {
		return optional.baseType.accept(this);
	}
	
	private void visitBool() {
		members.addOperator(BuiltinTypeMembers.BOOL_NOT);
	}
	
	private void visitByte() {
		
	}
	
	private void visitInt() {
		members.addOperator(INT_NEG);
		members.addOperator(INT_NOT);
		
		/*members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.PRE_INCREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.PRE_DECREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.POST_INCREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.POST_DECREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);*/

		members.addOperator(OperatorType.COMPARE, new ComparatorMember(members.type), TypeMemberPriority.SPECIFIED);
		
		members.addOperator(INT_ADD_INT);
		members.addOperator(add(LONG, LONG, castedTargetCall(LONG_ADD_LONG, INT_TO_LONG)));
		members.addOperator(add(FLOAT, FLOAT, castedTargetCall(FLOAT_ADD_FLOAT, INT_TO_FLOAT)));
		members.addOperator(add(DOUBLE, DOUBLE, castedTargetCall(DOUBLE_ADD_DOUBLE, INT_TO_DOUBLE)));
		
		members.addOperator(INT_SUB_INT);
		members.addOperator(sub(LONG, LONG, castedTargetCall(LONG_SUB_LONG, INT_TO_LONG)));
		members.addOperator(sub(FLOAT, FLOAT, castedTargetCall(FLOAT_SUB_FLOAT, INT_TO_FLOAT)));
		members.addOperator(sub(DOUBLE, DOUBLE, castedTargetCall(DOUBLE_SUB_DOUBLE, INT_TO_DOUBLE)));
		
		members.addOperator(INT_MUL_INT);
		members.addOperator(mul(LONG, LONG, castedTargetCall(LONG_MUL_LONG, INT_TO_LONG)));
		members.addOperator(mul(FLOAT, FLOAT, castedTargetCall(FLOAT_MUL_FLOAT, INT_TO_FLOAT)));
		members.addOperator(mul(DOUBLE, DOUBLE, castedTargetCall(DOUBLE_MUL_DOUBLE, INT_TO_DOUBLE)));
		
		members.addOperator(INT_DIV_INT);
		members.addOperator(div(LONG, LONG, castedTargetCall(LONG_DIV_LONG, INT_TO_LONG)));
		members.addOperator(div(FLOAT, FLOAT, castedTargetCall(FLOAT_DIV_FLOAT, INT_TO_FLOAT)));
		members.addOperator(div(DOUBLE, DOUBLE, castedTargetCall(DOUBLE_DIV_DOUBLE, INT_TO_DOUBLE)));
		
		members.addOperator(INT_MOD_INT);
		members.addOperator(mod(LONG, LONG, castedTargetCall(LONG_MOD_LONG, INT_TO_LONG)));
		
		members.addGetter(INT_GET_MIN_VALUE);
		members.addGetter(INT_GET_MAX_VALUE);
		
		members.addCaster(INT_TO_BYTE);
		members.addCaster(INT_TO_SBYTE);
		members.addCaster(INT_TO_SHORT);
		members.addCaster(INT_TO_USHORT);
		members.addCaster(INT_TO_UINT);
		members.addCaster(INT_TO_LONG);
		members.addCaster(INT_TO_ULONG);
		members.addCaster(INT_TO_FLOAT);
		members.addCaster(INT_TO_DOUBLE);
		members.addCaster(INT_TO_CHAR);
		members.addCaster(INT_TO_STRING);
	}

	private void visitUInt() {
		registerUnaryOperations();
		registerArithmeticOperations(UINT, UINT);
		registerArithmeticOperations(ULONG, ULONG);
		registerArithmeticOperations(FLOAT, FLOAT);
		registerArithmeticOperations(DOUBLE, DOUBLE);
		members.addGetter(new ConstantGetterMember("MIN_VALUE", position -> new ConstantUIntExpression(position, 0)), TypeMemberPriority.SPECIFIED);
		members.addGetter(new ConstantGetterMember("MAX_VALUE", position -> new ConstantUIntExpression(position, 0xFFFFFFFF)), TypeMemberPriority.SPECIFIED);
	}
	
	private void visitLong() {
		members.addOperator(LONG_NEG);
		members.addOperator(LONG_NOT);
		
		/*members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.PRE_INCREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.PRE_DECREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.POST_INCREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.POST_DECREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);*/

		members.addOperator(OperatorType.COMPARE, new ComparatorMember(members.type), TypeMemberPriority.SPECIFIED);
		
		members.addOperator(LONG_ADD_LONG);
		members.addOperator(add(FLOAT, FLOAT, castedTargetCall(FLOAT_ADD_FLOAT, LONG_TO_FLOAT)));
		members.addOperator(add(DOUBLE, DOUBLE, castedTargetCall(DOUBLE_ADD_DOUBLE, LONG_TO_DOUBLE)));
		
		members.addOperator(LONG_SUB_LONG);
		members.addOperator(sub(FLOAT, FLOAT, castedTargetCall(FLOAT_SUB_FLOAT, LONG_TO_FLOAT)));
		members.addOperator(sub(DOUBLE, DOUBLE, castedTargetCall(DOUBLE_SUB_DOUBLE, LONG_TO_DOUBLE)));
		
		members.addOperator(LONG_MUL_LONG);
		members.addOperator(mul(FLOAT, FLOAT, castedTargetCall(FLOAT_MUL_FLOAT, LONG_TO_FLOAT)));
		members.addOperator(mul(DOUBLE, DOUBLE, castedTargetCall(DOUBLE_MUL_DOUBLE, LONG_TO_DOUBLE)));
		
		members.addOperator(LONG_DIV_LONG);
		members.addOperator(div(FLOAT, FLOAT, castedTargetCall(FLOAT_DIV_FLOAT, LONG_TO_FLOAT)));
		members.addOperator(div(DOUBLE, DOUBLE, castedTargetCall(DOUBLE_DIV_DOUBLE, LONG_TO_DOUBLE)));
		
		members.addOperator(LONG_MOD_LONG);
		
		members.addGetter(LONG_GET_MIN_VALUE);
		members.addGetter(LONG_GET_MAX_VALUE);
		
		members.addCaster(LONG_TO_BYTE);
		members.addCaster(LONG_TO_SBYTE);
		members.addCaster(LONG_TO_SHORT);
		members.addCaster(LONG_TO_USHORT);
		members.addCaster(LONG_TO_INT);
		members.addCaster(LONG_TO_UINT);
		members.addCaster(LONG_TO_ULONG);
		members.addCaster(LONG_TO_FLOAT);
		members.addCaster(LONG_TO_DOUBLE);
		members.addCaster(LONG_TO_CHAR);
		members.addCaster(LONG_TO_STRING);
	}

	private void visitChar() {
		registerUnaryOperations();
		registerArithmeticOperations(CHAR, CHAR);
		registerArithmeticOperations(INT, INT);
		registerArithmeticOperations(LONG, LONG);
		registerArithmeticOperations(FLOAT, FLOAT);
		registerArithmeticOperations(DOUBLE, DOUBLE);
		members.addGetter(new ConstantGetterMember("MIN_VALUE", position -> new ConstantCharExpression(position, (char)0)), TypeMemberPriority.SPECIFIED);
		members.addGetter(new ConstantGetterMember("MAX_VALUE", position -> new ConstantCharExpression(position, (char)0xFFFF)), TypeMemberPriority.SPECIFIED);
		
		members.addCaster(new CasterMember(CodePosition.BUILTIN, 0, BasicTypeID.BYTE), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, 0, BasicTypeID.SBYTE), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.SHORT), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.USHORT), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.INT), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.UINT), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.LONG), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.ULONG), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.FLOAT), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.DOUBLE), TypeMemberPriority.SPECIFIED);
		members.addCaster(new CasterMember(CodePosition.BUILTIN, Modifiers.MODIFIER_IMPLICIT, BasicTypeID.STRING), TypeMemberPriority.SPECIFIED);
	}

	private void visitString() {
		FunctionHeader getIndexHeader = new FunctionHeader(CHAR, new FunctionParameter(INT));
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.INDEXGET, getIndexHeader), TypeMemberPriority.SPECIFIED);

		members.addGetter(new GetterMember(BUILTIN, 0, "length", INT), TypeMemberPriority.SPECIFIED);

		//FunctionHeader substringHeader = new FunctionHeader(STRING, new FunctionParameter(cache.getRegistry().getRange(INT, INT)));
		//members.addOperator(OperatorType.INDEXGET, new SubstringMember(substringHeader), TypeMemberPriority.SPECIFIED);
		
		members.addConstructor(new ConstructorMember(BUILTIN, 0, new FunctionHeader(VOID, new FunctionParameter(cache.getRegistry().getArray(CHAR, 1), "characters"))), TypeMemberPriority.SPECIFIED);
		
		members.addOperator(STRING_ADD_STRING);

		/*registerStringConcat(NULL);
		registerStringConcat(BOOL);
		registerStringConcat(SBYTE);
		registerStringConcat(BYTE);
		registerStringConcat(SHORT);
		registerStringConcat(USHORT);
		registerStringConcat(INT);
		registerStringConcat(UINT);
		registerStringConcat(LONG);
		registerStringConcat(ULONG);
		registerStringConcat(FLOAT);
		registerStringConcat(DOUBLE);
		registerStringConcat(CHAR);
		registerStringConcat(STRING);*/
	}

	private void registerStringConcat(ITypeID withType) {
		members.addOperator(STRING_ADD_STRING);
	}

	private void registerUnaryOperations() {
		FunctionHeader unaryHeader = new FunctionHeader(members.type);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.NEG, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.PRE_INCREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.PRE_DECREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.POST_INCREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.POST_DECREMENT, unaryHeader), TypeMemberPriority.SPECIFIED);

		members.addOperator(OperatorType.COMPARE, new ComparatorMember(members.type), TypeMemberPriority.SPECIFIED);
	}

	private void registerArithmeticOperations(ITypeID otherType, ITypeID resultType) {
		FunctionHeader binaryHeader = new FunctionHeader(resultType, new FunctionParameter(otherType));
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.ADD, binaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.SUB, binaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.MUL, binaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.DIV, binaryHeader), TypeMemberPriority.SPECIFIED);
		members.addOperator(new OperatorMember(BUILTIN, 0, OperatorType.MOD, binaryHeader), TypeMemberPriority.SPECIFIED);
	}
	
	private static OperatorMember add(ITypeID operand, ITypeID result, CallTranslator translator) {
		return new TranslatedOperatorMember(BUILTIN, 0, OperatorType.ADD, new FunctionHeader(result, new FunctionParameter(operand)), translator);
	}
	
	private static OperatorMember sub(ITypeID operand, ITypeID result, CallTranslator translator) {
		return new TranslatedOperatorMember(BUILTIN, 0, OperatorType.SUB, new FunctionHeader(result, new FunctionParameter(operand)), translator);
	}
	
	private static OperatorMember mul(ITypeID operand, ITypeID result, CallTranslator translator) {
		return new TranslatedOperatorMember(BUILTIN, 0, OperatorType.MUL, new FunctionHeader(result, new FunctionParameter(operand)), translator);
	}
	
	private static OperatorMember div(ITypeID operand, ITypeID result, CallTranslator translator) {
		return new TranslatedOperatorMember(BUILTIN, 0, OperatorType.DIV, new FunctionHeader(result, new FunctionParameter(operand)), translator);
	}
	
	private static OperatorMember mod(ITypeID operand, ITypeID result, CallTranslator translator) {
		return new TranslatedOperatorMember(BUILTIN, 0, OperatorType.MOD, new FunctionHeader(result, new FunctionParameter(operand)), translator);
	}
	
	private static CallTranslator castedTargetCall(FunctionalMember member, CasterMember caster) {
		return call -> member.call(call.position, caster.cast(call.position, call.target, true), call.arguments);
	}
}
