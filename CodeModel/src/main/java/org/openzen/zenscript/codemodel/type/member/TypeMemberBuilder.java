/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import java.util.Collections;
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
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.CallTranslator;
import org.openzen.zenscript.codemodel.expression.ConstantByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantDoubleExpression;
import org.openzen.zenscript.codemodel.expression.ConstantFloatExpression;
import org.openzen.zenscript.codemodel.expression.ConstantIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantLongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantSByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantShortExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantULongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUShortExpression;
import org.openzen.zenscript.codemodel.expression.EnumConstantExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.TranslatedOperatorMember;
import org.openzen.zenscript.codemodel.member.builtin.ArrayIteratorKeyValues;
import org.openzen.zenscript.codemodel.member.builtin.ArrayIteratorValues;
import org.openzen.zenscript.codemodel.member.builtin.AssocIterator;
import org.openzen.zenscript.codemodel.member.builtin.RangeIterator;
import org.openzen.zenscript.codemodel.member.builtin.StringCharIterator;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;
import org.openzen.zenscript.codemodel.type.ConstTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import static org.openzen.zenscript.codemodel.type.member.BuiltinID.*;
import org.openzen.zenscript.shared.CodePosition;
import static org.openzen.zenscript.shared.CodePosition.BUILTIN;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeMemberBuilder implements ITypeVisitor<Void> {
	private final GlobalTypeRegistry registry;
	private final TypeMembers members;
	private final LocalMemberCache cache;
	
	public TypeMemberBuilder(GlobalTypeRegistry registry, TypeMembers members, LocalMemberCache cache) {
		this.registry = registry;
		this.members = members;
		this.cache = cache;
	}
	
	private void processType(HighLevelDefinition definition, ITypeID type) {
		for (ExpansionDefinition expansion : cache.getExpansions()) {
			if (expansion.target == null)
				throw new CompileException(expansion.position, CompileExceptionCode.INTERNAL_ERROR, "Missing expansion target");
			
			Map<TypeParameter, ITypeID> mapping = matchType(type, expansion.target);
			if (mapping != null) {
				if (mapping.isEmpty()) {
					for (IDefinitionMember member : expansion.members)
						member.registerTo(members, TypeMemberPriority.SPECIFIED);
				} else {
					for (IDefinitionMember member : expansion.members)
						member.instance(registry, mapping).registerTo(members, TypeMemberPriority.SPECIFIED);
				}
			}
		}
		
		if (members.hasOperator(OperatorType.EQUALS)) {
			DefinitionMemberGroup group = members.getOrCreateGroup(OperatorType.EQUALS);
			DefinitionMemberGroup inverse = members.getOrCreateGroup(OperatorType.NOTEQUALS);
			for (TypeMember<FunctionalMember> method : group.getMethodMembers()) {
				if (!inverse.hasMethod(method.member.header)) {
					inverse.addMethod(notequals(definition, BuiltinID.AUTOOP_NOTEQUALS, method.member.header.parameters[0].type), TypeMemberPriority.SPECIFIED);
				}
			}
		}
	}
	
	private Map<TypeParameter, ITypeID> matchType(ITypeID type, ITypeID pattern) {
		if (type == pattern)
			return Collections.emptyMap();
		
		Map<TypeParameter, ITypeID> mapping = new HashMap<>();
		if (pattern.inferTypeParameters(cache, type, mapping))
			return mapping;
		
		return null;
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
			case SBYTE:
				visitSByte();
				break;
			case SHORT:
				visitShort();
				break;
			case USHORT:
				visitUShort();
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
			case ULONG:
				visitULong();
				break;
			case FLOAT:
				visitFloat();
				break;
			case DOUBLE:
				visitDouble();
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
		HighLevelDefinition definition = new ClassDefinition(BUILTIN, null, "", Modifiers.EXPORT);
		ITypeID baseType = array.elementType;
		int dimension = array.dimension;

		FunctionParameter[] indexGetParameters = new FunctionParameter[dimension];
		for (int i = 0; i < indexGetParameters.length; i++)
			indexGetParameters[i] = new FunctionParameter(INT);
		
		members.addOperator(new OperatorMember(
				BUILTIN,
				definition,
				0,
				OperatorType.INDEXGET,
				new FunctionHeader(baseType, indexGetParameters),
				ARRAY_INDEXGET));
		
		if (dimension == 1) {
			FunctionHeader sliceHeader = new FunctionHeader(array, new FunctionParameter(cache.getRegistry().getRange(INT, INT), "range"));
			members.addOperator(new OperatorMember(
					BUILTIN,
					definition,
					0,
					OperatorType.INDEXGET,
					sliceHeader,
					ARRAY_INDEXGETRANGE));
		}

		FunctionHeader containsHeader = new FunctionHeader(BOOL, new FunctionParameter(baseType, "value"));
		members.addOperator(new OperatorMember(
				BUILTIN,
				definition,
				0,
				OperatorType.CONTAINS,
				containsHeader,
				ARRAY_CONTAINS));
		
		if (baseType.hasDefaultValue()) {
			members.addConstructor(new ConstructorMember(
					BUILTIN,
					definition,
					0,
					new FunctionHeader(VOID, indexGetParameters),
					ARRAY_CONSTRUCTOR_SIZED));
		}

		FunctionParameter[] initialValueConstructorParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			initialValueConstructorParameters[i] = new FunctionParameter(INT);
		initialValueConstructorParameters[dimension] = new FunctionParameter(baseType);
		FunctionHeader initialValueConstructorHeader = new FunctionHeader(VOID, initialValueConstructorParameters);
		members.addConstructor(new ConstructorMember(
				BUILTIN,
				definition,
				0,
				initialValueConstructorHeader,
				ARRAY_CONSTRUCTOR_INITIAL_VALUE));
		
		FunctionParameter[] lambdaConstructorParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			lambdaConstructorParameters[i] = new FunctionParameter(INT, null);
		
		FunctionHeader lambdaConstructorFunction = new FunctionHeader(baseType, indexGetParameters);
		lambdaConstructorParameters[dimension] = new FunctionParameter(cache.getRegistry().getFunction(lambdaConstructorFunction), null);
		FunctionHeader lambdaConstructorHeader = new FunctionHeader(VOID, lambdaConstructorParameters);
		members.addConstructor(new ConstructorMember(
				BUILTIN,
				definition,
				0,
				lambdaConstructorHeader,
				ARRAY_CONSTRUCTOR_LAMBDA));
		
		{
			TypeParameter mappedConstructorParameter = new TypeParameter(BUILTIN, "T");
			FunctionHeader mappedConstructorHeaderWithoutIndex = new FunctionHeader(baseType, registry.getGeneric(mappedConstructorParameter));
			FunctionHeader mappedConstructorFunctionWithoutIndex = new FunctionHeader(
					new TypeParameter[] { mappedConstructorParameter },
					VOID,
					null,
					new FunctionParameter(registry.getArray(registry.getGeneric(mappedConstructorParameter), dimension), "original"),
					new FunctionParameter(registry.getFunction(mappedConstructorHeaderWithoutIndex), "projection"));
			members.addConstructor(new ConstructorMember(BUILTIN, definition, Modifiers.PUBLIC, mappedConstructorFunctionWithoutIndex, ARRAY_CONSTRUCTOR_PROJECTED));
		}
		
		{
			TypeParameter mappedConstructorParameter = new TypeParameter(BUILTIN, "T");
			FunctionParameter[] projectionParameters = new FunctionParameter[dimension + 1];
			for (int i = 0; i < dimension; i++)
				projectionParameters[i] = new FunctionParameter(INT);
			projectionParameters[dimension] = new FunctionParameter(registry.getGeneric(mappedConstructorParameter));
			
			FunctionHeader mappedConstructorHeaderWithIndex = new FunctionHeader(baseType, projectionParameters);
			FunctionHeader mappedConstructorFunctionWithIndex = new FunctionHeader(
					new TypeParameter[] { mappedConstructorParameter },
					VOID,
					null,
					new FunctionParameter(registry.getArray(registry.getGeneric(mappedConstructorParameter), dimension), "original"),
					new FunctionParameter(registry.getFunction(mappedConstructorHeaderWithIndex), "projection"));
			members.addConstructor(new ConstructorMember(BUILTIN, definition, Modifiers.PUBLIC, mappedConstructorFunctionWithIndex, ARRAY_CONSTRUCTOR_PROJECTED_INDEXED));
		}
		
		FunctionParameter[] indexSetParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			indexSetParameters[i] = new FunctionParameter(INT, null);
		indexSetParameters[dimension] = new FunctionParameter(baseType, null);

		FunctionHeader indexSetHeader = new FunctionHeader(VOID, indexSetParameters);
		members.addOperator(new OperatorMember(
				BUILTIN,
				definition,
				0,
				OperatorType.INDEXSET,
				indexSetHeader,
				ARRAY_INDEXSET));

		if (dimension == 1) {
			members.addGetter(getter(definition, ARRAY_LENGTH, "length", INT));
		}

		members.addGetter(getter(definition, ARRAY_ISEMPTY, "isEmpty", BOOL));
		members.addGetter(getter(definition, ARRAY_HASHCODE, "objectHashCode", INT));
		members.addIterator(new ArrayIteratorKeyValues(array), TypeMemberPriority.SPECIFIED);
		members.addIterator(new ArrayIteratorValues(array), TypeMemberPriority.SPECIFIED);
		
		members.addOperator(equals(definition, ARRAY_EQUALS, array));
		members.addOperator(notequals(definition, ARRAY_NOTEQUALS, array));
		members.addOperator(same(definition, ARRAY_SAME, array));
		members.addOperator(notsame(definition, ARRAY_NOTSAME, array));
		
		processType(definition, array);
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		ITypeID keyType = assoc.keyType;
		ITypeID valueType = assoc.valueType;
		
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "", Modifiers.EXPORT);
		
		members.addConstructor(constructor(builtin, ASSOC_CONSTRUCTOR));

		members.addOperator(indexGet(builtin, ASSOC_INDEXGET, keyType, valueType));
		members.addOperator(indexSet(builtin, ASSOC_INDEXSET, keyType, valueType));
		
		members.addMethod(method(builtin, ASSOC_GETORDEFAULT, "getOrDefault", valueType, keyType, valueType));
		
		members.addOperator(new OperatorMember(
				BUILTIN,
				builtin,
				0,
				OperatorType.CONTAINS,
				new FunctionHeader(BOOL, new FunctionParameter(keyType, "key")),
				ASSOC_CONTAINS));
		
		members.addGetter(getter(builtin, BuiltinID.ASSOC_SIZE, "size", INT));
		members.addGetter(getter(builtin, BuiltinID.ASSOC_ISEMPTY, "isEmpty", BOOL));
		members.addGetter(getter(builtin, BuiltinID.ASSOC_KEYS, "keys", cache.getRegistry().getArray(keyType, 1)));
		members.addGetter(getter(builtin, BuiltinID.ASSOC_VALUES, "values", cache.getRegistry().getArray(valueType, 1)));
		members.addGetter(getter(builtin, BuiltinID.ASSOC_HASHCODE, "objectHashCode", BasicTypeID.INT));
		
		members.addIterator(new AssocIterator(assoc), TypeMemberPriority.SPECIFIED);
		
		members.addOperator(equals(builtin, BuiltinID.ASSOC_EQUALS, assoc));
		members.addOperator(notequals(builtin, BuiltinID.ASSOC_NOTEQUALS, assoc));
		members.addOperator(same(builtin, BuiltinID.ASSOC_SAME, assoc));
		members.addOperator(notsame(builtin, BuiltinID.ASSOC_NOTSAME, assoc));
		
		processType(builtin, assoc);
		return null;
	}
	
	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		ITypeID valueType = map.value;
		
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "", Modifiers.EXPORT);
		members.addConstructor(constructor(builtin, GENERICMAP_CONSTRUCTOR));
		members.addMethod(new MethodMember(
				BUILTIN,
				builtin,
				0,
				"getOptional",
				new FunctionHeader(new TypeParameter[] { map.key }, registry.getOptional(valueType), null, new FunctionParameter[0]),
				GENERICMAP_GETOPTIONAL));
		members.addMethod(new MethodMember(
				BUILTIN,
				builtin,
				0,
				"put",
				new FunctionHeader(new TypeParameter[] { map.key }, BasicTypeID.VOID, null, new FunctionParameter(valueType)),
				GENERICMAP_PUT));
		members.addMethod(new MethodMember(
				BUILTIN,
				builtin,
				0,
				"contains",
				new FunctionHeader(new TypeParameter[] { map.key }, BasicTypeID.BOOL, null, new FunctionParameter[0]),
				GENERICMAP_CONTAINS));
		members.addGetter(getter(builtin, GENERICMAP_HASHCODE, "objectHashCode", INT));
		
		members.addOperator(equals(builtin, GENERICMAP_EQUALS, map));
		members.addOperator(notequals(builtin, GENERICMAP_NOTEQUALS, map));
		members.addOperator(same(builtin, GENERICMAP_SAME, map));
		members.addOperator(notsame(builtin, GENERICMAP_NOTSAME, map));
		
		processType(builtin, map);
		return null;
	}
	
	@Override
	public Void visitIterator(IteratorTypeID iterator) {
		return null;
	}

	@Override
	public Void visitFunction(FunctionTypeID function) {
		FunctionDefinition builtin = new FunctionDefinition(BUILTIN, null, "", Modifiers.EXPORT, function.header);
		members.addCaller(new CallerMember(BUILTIN, builtin, 0, function.header, FUNCTION_CALL), TypeMemberPriority.SPECIFIED);
		members.addOperator(same(builtin, FUNCTION_SAME, function));
		members.addOperator(notsame(builtin, FUNCTION_NOTSAME, function));
		
		processType(builtin, function);
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID type) {
		HighLevelDefinition definition = type.definition;
		if (type.hasTypeParameters() || !type.outerTypeParameters.isEmpty()) {
			Map<TypeParameter, ITypeID> mapping = new HashMap<>();
			if (type.typeParameters != null) {
				if (definition.genericParameters == null)
					System.out.println("Type parameters but no generic parameters");
				else
					for (int i = 0; i < type.typeParameters.length; i++)
						mapping.put(definition.genericParameters[i], type.typeParameters[i]);
			}
			
			if (!type.definition.isStatic())
				for (Map.Entry<TypeParameter, ITypeID> entry : type.outerTypeParameters.entrySet())
					mapping.put(entry.getKey(), entry.getValue());
			
			for (IDefinitionMember member : definition.members) {
				member.instance(cache.getRegistry(), mapping).registerTo(members, TypeMemberPriority.SPECIFIED);
			}
			
			if (definition instanceof VariantDefinition) {
				VariantDefinition variant = (VariantDefinition) definition;
				for (VariantDefinition.Option option : variant.options)
					members.addVariantOption(option.instance(registry, mapping));
			}
		} else {
			for (IDefinitionMember member : definition.members) {
				member.registerTo(members, TypeMemberPriority.SPECIFIED);
			}
			
			if (definition instanceof VariantDefinition) {
				VariantDefinition variant = (VariantDefinition) definition;
				for (VariantDefinition.Option option : variant.options)
					members.addVariantOption(option);
			}
		}
		
		if (definition instanceof EnumDefinition) {
			EnumDefinition enumDef = (EnumDefinition) definition;
			for (EnumConstantMember constant : enumDef.enumConstants) {
				members.addEnumMember(constant, TypeMemberPriority.SPECIFIED);
			}
		}

		DefinitionMemberGroup constructors = members.getOrCreateGroup(OperatorType.CONSTRUCTOR);
		if (constructors.getMethodMembers().isEmpty()) {
			if (definition instanceof ClassDefinition) {
				// add default constructor (TODO: only works if all fields have a default value)
				constructors.addMethod(new ConstructorMember(
						BUILTIN,
						definition,
						Modifiers.PUBLIC,
						new FunctionHeader(VOID),
						CLASS_DEFAULT_CONSTRUCTOR), TypeMemberPriority.SPECIFIED);
			} else if (definition instanceof StructDefinition) {
				// add default struct constructors (TODO: only works if all fields have a default value)
				constructors.addMethod(new ConstructorMember(
						BUILTIN,
						definition,
						Modifiers.PUBLIC,
						new FunctionHeader(VOID),
						STRUCT_EMPTY_CONSTRUCTOR), TypeMemberPriority.SPECIFIED);
				
				List<FieldMember> fields = definition.getFields();
				if (!fields.isEmpty()) {
					FunctionParameter[] parameters = new FunctionParameter[fields.size()];
					for (int i = 0; i < parameters.length; i++) {
						FieldMember field = fields.get(i);
						parameters[i] = new FunctionParameter(field.type, field.name, field.initializer, false);
					}
					constructors.addMethod(new ConstructorMember(
							BUILTIN,
							definition,
							Modifiers.PUBLIC,
							new FunctionHeader(VOID, parameters),
							STRUCT_VALUE_CONSTRUCTOR), TypeMemberPriority.SPECIFIED);
				}
			} else if (definition instanceof EnumDefinition) {
				// add default constructor
				constructors.addMethod(new ConstructorMember(
						BUILTIN,
						definition,
						Modifiers.PRIVATE,
						new FunctionHeader(VOID),
						ENUM_EMPTY_CONSTRUCTOR), TypeMemberPriority.SPECIFIED);
			}
		}
		
		if (definition instanceof EnumDefinition) {
			members.addGetter(getter(definition, ENUM_NAME, "name", STRING));
			members.addGetter(getter(definition, ENUM_ORDINAL, "ordinal", INT));
			
			List<EnumConstantMember> enumConstants = ((EnumDefinition) definition).enumConstants;
			Expression[] constValues = new Expression[enumConstants.size()];
			for (int i = 0; i < constValues.length; i++)
				constValues[i] = new EnumConstantExpression(BUILTIN, type, enumConstants.get(i));
			
			members.addConst(constant(definition, ENUM_VALUES, "values", new ArrayExpression(BUILTIN, constValues, registry.getArray(type, 1))));
			members.addOperator(compare(definition, ENUM_COMPARE, type));
			
			if (!members.canCast(BasicTypeID.STRING)) {
				members.addCaster(castImplicit(definition, ENUM_TO_STRING, STRING));
			}
		}
		
		if (type.superType != null) {
			cache.get(type.superType).copyMembersTo(type.definition.position, members, TypeMemberPriority.INHERITED);
		} else {
			members.addGetter(getter(definition, OBJECT_HASHCODE, "objectHashCode", BasicTypeID.INT));
		}
		
		members.addOperator(same(definition, OBJECT_SAME, type));
		members.addOperator(notsame(definition, OBJECT_NOTSAME, type));
		
		processType(definition, type);
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

		ClassDefinition definition = new ClassDefinition(BUILTIN, null, "", Modifiers.EXPORT);
		members.addGetter(getter(definition, RANGE_FROM, "from", fromType));
		members.addGetter(getter(definition, RANGE_TO, "to", toType));
		if (range.from == range.to && (range.from == BasicTypeID.BYTE
				|| range.from == SBYTE
				|| range.from == SHORT
				|| range.from == USHORT
				|| range.from == INT
				|| range.from == UINT
				|| range.from == LONG
				|| range.from == ULONG)) {
			members.addIterator(new RangeIterator(range), TypeMemberPriority.SPECIFIED);
		}
		
		processType(definition, range);
		return null;
	}

	@Override
	public Void visitConst(ConstTypeID type) {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "const", Modifiers.EXPORT, null);
		type.baseType.accept(this);
		processType(builtin, type);
		return null;
	}

	@Override
	public Void visitOptional(OptionalTypeID optional) {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "optional", Modifiers.EXPORT, null);
		optional.baseType.accept(this);
		processType(builtin, optional);
		return null;
	}
	
	private void visitBool() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "bool", Modifiers.EXPORT, null);
		members.addOperator(not(builtin, BOOL_NOT, BOOL));
		members.addOperator(and(builtin, BOOL_AND, BOOL, BOOL));
		members.addOperator(or(builtin, BOOL_OR, BOOL, BOOL));
		members.addOperator(xor(builtin, BOOL_XOR, BOOL, BOOL));
		members.addOperator(equals(builtin, BOOL_EQUALS, BOOL));
		members.addOperator(notequals(builtin, BOOL_NOTEQUALS, BOOL));
		
		members.addCaster(castExplicit(builtin, BOOL_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, BOOL_PARSE, "parse", BOOL, STRING));
		
		processType(builtin, BOOL);
	}
	
	private void visitByte() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "byte", Modifiers.EXPORT, null);
		
		members.addOperator(invert(builtin, BYTE_NOT, BYTE));
		members.addOperator(inc(builtin, BYTE_INC, BYTE));
		members.addOperator(dec(builtin, BYTE_DEC, BYTE));
		members.addOperator(add(builtin, BYTE_ADD_BYTE, BYTE, BYTE));
		members.addOperator(sub(builtin, BYTE_SUB_BYTE, BYTE, BYTE));
		members.addOperator(mul(builtin, BYTE_MUL_BYTE, BYTE, BYTE));
		members.addOperator(div(builtin, BYTE_DIV_BYTE, BYTE, BYTE));
		members.addOperator(mod(builtin, BYTE_MOD_BYTE, BYTE, BYTE));
		members.addOperator(and(builtin, BYTE_AND_BYTE, BYTE, BYTE));
		members.addOperator(or(builtin, BYTE_OR_BYTE, BYTE, BYTE));
		members.addOperator(xor(builtin, BYTE_XOR_BYTE, BYTE, BYTE));
		members.addOperator(compare(builtin, BYTE_COMPARE, BYTE));
		
		members.addCaster(castImplicit(builtin, BYTE_TO_SBYTE, SBYTE));
		members.addCaster(castImplicit(builtin, BYTE_TO_SHORT, SHORT));
		members.addCaster(castImplicit(builtin, BYTE_TO_USHORT, USHORT));
		members.addCaster(castImplicit(builtin, BYTE_TO_INT, INT));
		members.addCaster(castImplicit(builtin, BYTE_TO_UINT, UINT));
		members.addCaster(castImplicit(builtin, BYTE_TO_LONG, LONG));
		members.addCaster(castImplicit(builtin, BYTE_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, BYTE_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, BYTE_TO_DOUBLE, DOUBLE));
		members.addCaster(castExplicit(builtin, BYTE_TO_CHAR, CHAR));
		members.addCaster(castImplicit(builtin, BYTE_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, BYTE_PARSE, "parse", BYTE, STRING));
		members.addMethod(staticMethod(builtin, BYTE_PARSE_WITH_BASE, "parse", BYTE, STRING, INT));
		
		members.addConst(constant(builtin, BYTE_GET_MIN_VALUE, "MIN_VALUE", new ConstantByteExpression(BUILTIN, 0)));
		members.addConst(constant(builtin, BYTE_GET_MAX_VALUE, "MAX_VALUE", new ConstantByteExpression(BUILTIN, 255)));
		
		processType(builtin, BYTE);
	}
	
	private void visitSByte() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "sbyte", Modifiers.EXPORT, null);
		
		members.addOperator(invert(builtin, SBYTE_NOT, SBYTE));
		members.addOperator(neg(builtin, SBYTE_NEG, SBYTE));
		members.addOperator(inc(builtin, SBYTE_INC, SBYTE));
		members.addOperator(dec(builtin, SBYTE_DEC, SBYTE));
		members.addOperator(add(builtin, SBYTE_ADD_SBYTE, SBYTE, SBYTE));
		members.addOperator(sub(builtin, SBYTE_SUB_SBYTE, SBYTE, SBYTE));
		members.addOperator(mul(builtin, SBYTE_MUL_SBYTE, SBYTE, SBYTE));
		members.addOperator(div(builtin, SBYTE_DIV_SBYTE, SBYTE, SBYTE));
		members.addOperator(mod(builtin, SBYTE_MOD_SBYTE, SBYTE, SBYTE));
		members.addOperator(and(builtin, SBYTE_AND_SBYTE, SBYTE, SBYTE));
		members.addOperator(or(builtin, SBYTE_OR_SBYTE, SBYTE, SBYTE));
		members.addOperator(xor(builtin, SBYTE_XOR_SBYTE, SBYTE, SBYTE));
		members.addOperator(compare(builtin, SBYTE_COMPARE, SBYTE));
		
		members.addCaster(castImplicit(builtin, SBYTE_TO_BYTE, BYTE));
		members.addCaster(castImplicit(builtin, SBYTE_TO_SHORT, SHORT));
		members.addCaster(castImplicit(builtin, SBYTE_TO_USHORT, USHORT));
		members.addCaster(castImplicit(builtin, SBYTE_TO_INT, INT));
		members.addCaster(castImplicit(builtin, SBYTE_TO_UINT, UINT));
		members.addCaster(castImplicit(builtin, SBYTE_TO_LONG, LONG));
		members.addCaster(castImplicit(builtin, SBYTE_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, SBYTE_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, SBYTE_TO_DOUBLE, DOUBLE));
		members.addCaster(castExplicit(builtin, SBYTE_TO_CHAR, CHAR));
		members.addCaster(castImplicit(builtin, SBYTE_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, SBYTE_PARSE, "parse", SBYTE, STRING));
		members.addMethod(staticMethod(builtin, SBYTE_PARSE_WITH_BASE, "parse", SBYTE, STRING, INT));
		
		members.addConst(constant(builtin, SBYTE_GET_MIN_VALUE, "MIN_VALUE", new ConstantSByteExpression(BUILTIN, Byte.MIN_VALUE)));
		members.addConst(constant(builtin, SBYTE_GET_MAX_VALUE, "MAX_VALUE", new ConstantSByteExpression(BUILTIN, Byte.MAX_VALUE)));
		
		processType(builtin, SBYTE);
	}
	
	private void visitShort() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "short", Modifiers.EXPORT, null);
		
		members.addOperator(invert(builtin, SHORT_NOT, SHORT));
		members.addOperator(neg(builtin, SHORT_NEG, SHORT));
		members.addOperator(inc(builtin, SHORT_INC, SHORT));
		members.addOperator(dec(builtin, SHORT_DEC, SHORT));
		members.addOperator(add(builtin, SHORT_ADD_SHORT, SHORT, SHORT));
		members.addOperator(sub(builtin, SHORT_SUB_SHORT, SHORT, SHORT));
		members.addOperator(mul(builtin, SHORT_MUL_SHORT, SHORT, SHORT));
		members.addOperator(div(builtin, SHORT_DIV_SHORT, SHORT, SHORT));
		members.addOperator(mod(builtin, SHORT_MOD_SHORT, SHORT, SHORT));
		members.addOperator(and(builtin, SHORT_AND_SHORT, SHORT, SHORT));
		members.addOperator(or(builtin, SHORT_OR_SHORT, SHORT, SHORT));
		members.addOperator(xor(builtin, SHORT_XOR_SHORT, SHORT, SHORT));
		members.addOperator(compare(builtin, SHORT_COMPARE, SHORT));
		
		members.addCaster(castExplicit(builtin, SHORT_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, SHORT_TO_SBYTE, SBYTE));
		members.addCaster(castImplicit(builtin, SHORT_TO_USHORT, USHORT));
		members.addCaster(castImplicit(builtin, SHORT_TO_INT, INT));
		members.addCaster(castImplicit(builtin, SHORT_TO_UINT, UINT));
		members.addCaster(castImplicit(builtin, SHORT_TO_LONG, LONG));
		members.addCaster(castImplicit(builtin, SHORT_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, SHORT_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, SHORT_TO_DOUBLE, DOUBLE));
		members.addCaster(castExplicit(builtin, SHORT_TO_CHAR, CHAR));
		members.addCaster(castImplicit(builtin, SHORT_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, SHORT_PARSE, "parse", SHORT, STRING));
		members.addMethod(staticMethod(builtin, SHORT_PARSE_WITH_BASE, "parse", SHORT, STRING, INT));
		
		members.addConst(constant(builtin, SHORT_GET_MIN_VALUE, "MIN_VALUE", new ConstantShortExpression(BUILTIN, Short.MIN_VALUE)));
		members.addConst(constant(builtin, SHORT_GET_MAX_VALUE, "MAX_VALUE", new ConstantShortExpression(BUILTIN, Short.MAX_VALUE)));
		
		processType(builtin, SHORT);
	}
	
	private void visitUShort() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "ushort", Modifiers.EXPORT, null);
		
		members.addOperator(invert(builtin, USHORT_NOT, USHORT));
		members.addOperator(inc(builtin, USHORT_INC, USHORT));
		members.addOperator(dec(builtin, USHORT_DEC, USHORT));
		members.addOperator(add(builtin, USHORT_ADD_USHORT, USHORT, USHORT));
		members.addOperator(sub(builtin, USHORT_SUB_USHORT, USHORT, USHORT));
		members.addOperator(mul(builtin, USHORT_MUL_USHORT, USHORT, USHORT));
		members.addOperator(div(builtin, USHORT_DIV_USHORT, USHORT, USHORT));
		members.addOperator(mod(builtin, USHORT_MOD_USHORT, USHORT, USHORT));
		members.addOperator(and(builtin, USHORT_AND_USHORT, USHORT, USHORT));
		members.addOperator(or(builtin, USHORT_OR_USHORT, USHORT, USHORT));
		members.addOperator(xor(builtin, USHORT_XOR_USHORT, USHORT, USHORT));
		members.addOperator(compare(builtin, USHORT_COMPARE, USHORT));
		
		members.addCaster(castExplicit(builtin, USHORT_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, USHORT_TO_SBYTE, SBYTE));
		members.addCaster(castImplicit(builtin, USHORT_TO_SHORT, SHORT));
		members.addCaster(castImplicit(builtin, USHORT_TO_INT, INT));
		members.addCaster(castImplicit(builtin, USHORT_TO_UINT, UINT));
		members.addCaster(castImplicit(builtin, USHORT_TO_LONG, LONG));
		members.addCaster(castImplicit(builtin, USHORT_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, USHORT_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, USHORT_TO_DOUBLE, DOUBLE));
		members.addCaster(castExplicit(builtin, USHORT_TO_CHAR, CHAR));
		members.addCaster(castImplicit(builtin, USHORT_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, USHORT_PARSE, "parse", USHORT, STRING));
		members.addMethod(staticMethod(builtin, USHORT_PARSE_WITH_BASE, "parse", USHORT, STRING, INT));
		
		members.addConst(constant(builtin, USHORT_GET_MIN_VALUE, "MIN_VALUE", new ConstantUShortExpression(BUILTIN, 0)));
		members.addConst(constant(builtin, USHORT_GET_MAX_VALUE, "MAX_VALUE", new ConstantUShortExpression(BUILTIN, 65535)));
		
		processType(builtin, USHORT);
	}
	
	private void visitInt() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "int", Modifiers.EXPORT, null);
		
		members.addOperator(invert(builtin, INT_NOT, INT));
		members.addOperator(neg(builtin, INT_NEG, INT));
		members.addOperator(inc(builtin, INT_DEC, INT));
		members.addOperator(dec(builtin, INT_INC, INT));

		members.addOperator(add(builtin, INT_ADD_INT, INT, INT));
		members.addOperator(add(builtin, LONG_ADD_LONG, LONG, LONG, INT_TO_LONG));
		members.addOperator(add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, INT_TO_FLOAT));
		members.addOperator(add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, INT_TO_DOUBLE));
		
		members.addOperator(sub(builtin, INT_SUB_INT, INT, INT));
		members.addOperator(sub(builtin, LONG_SUB_LONG, LONG, LONG, INT_TO_LONG));
		members.addOperator(sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, INT_TO_FLOAT));
		members.addOperator(sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, INT_TO_DOUBLE));
		
		members.addOperator(mul(builtin, INT_MUL_INT, INT, INT));
		members.addOperator(mul(builtin, LONG_MUL_LONG, LONG, LONG, INT_TO_LONG));
		members.addOperator(mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, INT_TO_FLOAT));
		members.addOperator(mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, INT_TO_DOUBLE));
		
		members.addOperator(div(builtin, INT_DIV_INT, INT, INT));
		members.addOperator(div(builtin, LONG_DIV_LONG, LONG, LONG, INT_TO_LONG));
		members.addOperator(div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, INT_TO_FLOAT));
		members.addOperator(div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, INT_TO_DOUBLE));
		
		members.addOperator(mod(builtin, INT_MOD_INT, INT, INT));
		members.addOperator(mod(builtin, LONG_MOD_LONG, LONG, LONG, INT_TO_LONG));
		
		members.addOperator(or(builtin, INT_OR_INT, INT, INT));
		members.addOperator(or(builtin, LONG_OR_LONG, LONG, LONG, INT_TO_LONG));
		members.addOperator(and(builtin, INT_AND_INT, INT, INT));
		members.addOperator(and(builtin, LONG_AND_LONG, LONG, LONG, INT_TO_LONG));
		members.addOperator(xor(builtin, INT_XOR_INT, INT, INT));
		members.addOperator(xor(builtin, LONG_XOR_LONG, LONG, LONG, INT_TO_LONG));
		
		members.addOperator(shl(builtin, INT_SHL, INT, INT));
		members.addOperator(shr(builtin, INT_SHR, INT, INT));
		members.addOperator(ushr(builtin, INT_USHR, INT, INT));
		
		members.addOperator(compare(builtin, INT_COMPARE, INT));
		members.addOperator(compare(builtin, LONG_COMPARE, LONG, INT_TO_LONG));
		members.addOperator(compare(builtin, FLOAT_COMPARE, FLOAT, INT_TO_FLOAT));
		members.addOperator(compare(builtin, DOUBLE_COMPARE, DOUBLE, INT_TO_DOUBLE));
		
		members.addConst(constant(builtin, INT_GET_MIN_VALUE, "MIN_VALUE", new ConstantIntExpression(BUILTIN, Integer.MIN_VALUE)));
		members.addConst(constant(builtin, INT_GET_MAX_VALUE, "MAX_VALUE", new ConstantIntExpression(BUILTIN, Integer.MAX_VALUE)));
		
		members.addCaster(castExplicit(builtin, INT_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, INT_TO_SBYTE, SBYTE));
		members.addCaster(castExplicit(builtin, INT_TO_SHORT, SHORT));
		members.addCaster(castExplicit(builtin, INT_TO_USHORT, USHORT));
		members.addCaster(castImplicit(builtin, INT_TO_UINT, UINT));
		members.addCaster(castImplicit(builtin, INT_TO_LONG, LONG));
		members.addCaster(castImplicit(builtin, INT_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, INT_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, INT_TO_DOUBLE, DOUBLE));
		members.addCaster(castExplicit(builtin, INT_TO_CHAR, CHAR));
		members.addCaster(castImplicit(builtin, INT_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, INT_PARSE, "parse", INT, STRING));
		members.addMethod(staticMethod(builtin, INT_PARSE_WITH_BASE, "parse", INT, STRING, INT));
		
		members.addMethod(method(builtin, INT_COUNT_LOW_ZEROES, "countLowZeroes", INT));
		members.addMethod(method(builtin, INT_COUNT_HIGH_ZEROES, "countHighZeroes", INT));
		members.addMethod(method(builtin, INT_COUNT_LOW_ONES, "countLowOnes", INT));
		members.addMethod(method(builtin, INT_COUNT_HIGH_ONES, "countHighOnes", INT));
		
		ITypeID optionalInt = registry.getOptional(INT);
		members.addGetter(getter(builtin, INT_HIGHEST_ONE_BIT, "highestOneBit", optionalInt));
		members.addGetter(getter(builtin, INT_LOWEST_ONE_BIT, "lowestOneBit", optionalInt));
		members.addGetter(getter(builtin, INT_HIGHEST_ZERO_BIT, "highestZeroBit", optionalInt));
		members.addGetter(getter(builtin, INT_LOWEST_ZERO_BIT, "lowestZeroBit", optionalInt));
		members.addGetter(getter(builtin, INT_BIT_COUNT, "bitCount", INT));
		
		processType(builtin, INT);
	}

	private void visitUInt() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "uint", Modifiers.EXPORT, null);
		
		members.addOperator(invert(builtin, UINT_NOT, INT));
		members.addOperator(inc(builtin, UINT_DEC, INT));
		members.addOperator(dec(builtin, UINT_INC, INT));

		members.addOperator(add(builtin, UINT_ADD_UINT, UINT, UINT));
		members.addOperator(add(builtin, ULONG_ADD_ULONG, ULONG, ULONG, UINT_TO_ULONG));
		members.addOperator(add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, UINT_TO_FLOAT));
		members.addOperator(add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, UINT_TO_DOUBLE));
		
		members.addOperator(sub(builtin, UINT_SUB_UINT, UINT, UINT));
		members.addOperator(sub(builtin, ULONG_SUB_ULONG, ULONG, ULONG, UINT_TO_ULONG));
		members.addOperator(sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, UINT_TO_FLOAT));
		members.addOperator(sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, UINT_TO_DOUBLE));
		
		members.addOperator(mul(builtin, UINT_MUL_UINT, UINT, UINT));
		members.addOperator(mul(builtin, ULONG_MUL_ULONG, ULONG, ULONG, UINT_TO_ULONG));
		members.addOperator(mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, UINT_TO_FLOAT));
		members.addOperator(mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, UINT_TO_DOUBLE));
		
		members.addOperator(div(builtin, UINT_DIV_UINT, UINT, UINT));
		members.addOperator(div(builtin, ULONG_DIV_ULONG, ULONG, ULONG, UINT_TO_ULONG));
		members.addOperator(div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, UINT_TO_FLOAT));
		members.addOperator(div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, UINT_TO_DOUBLE));
		
		members.addOperator(mod(builtin, UINT_MOD_UINT, UINT, UINT));
		members.addOperator(mod(builtin, ULONG_MOD_ULONG, ULONG, ULONG, UINT_TO_ULONG));
		
		members.addOperator(or(builtin, UINT_OR_UINT, UINT, UINT));
		members.addOperator(or(builtin, ULONG_OR_ULONG, ULONG, ULONG, UINT_TO_ULONG));
		members.addOperator(and(builtin, UINT_AND_UINT, UINT, UINT));
		members.addOperator(and(builtin, ULONG_AND_ULONG, ULONG, ULONG, UINT_TO_ULONG));
		members.addOperator(xor(builtin, UINT_XOR_UINT, UINT, UINT));
		members.addOperator(xor(builtin, ULONG_XOR_ULONG, ULONG, ULONG, UINT_TO_ULONG));
		
		members.addOperator(shl(builtin, UINT_SHL, UINT, UINT));
		members.addOperator(shr(builtin, UINT_SHR, UINT, UINT));
		
		members.addOperator(compare(builtin, UINT_COMPARE, UINT));
		members.addOperator(compare(builtin, ULONG_COMPARE, ULONG, UINT_TO_LONG));
		members.addOperator(compare(builtin, FLOAT_COMPARE, FLOAT, UINT_TO_FLOAT));
		members.addOperator(compare(builtin, DOUBLE_COMPARE, DOUBLE, UINT_TO_DOUBLE));
		
		members.addConst(constant(builtin, UINT_GET_MIN_VALUE, "MIN_VALUE", new ConstantUIntExpression(BUILTIN, 0)));
		members.addConst(constant(builtin, UINT_GET_MAX_VALUE, "MAX_VALUE", new ConstantUIntExpression(BUILTIN, -1)));
		
		members.addCaster(castExplicit(builtin, UINT_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, UINT_TO_SBYTE, SBYTE));
		members.addCaster(castExplicit(builtin, UINT_TO_SHORT, SHORT));
		members.addCaster(castExplicit(builtin, UINT_TO_USHORT, USHORT));
		members.addCaster(castImplicit(builtin, UINT_TO_INT, INT));
		members.addCaster(castImplicit(builtin, UINT_TO_LONG, LONG));
		members.addCaster(castImplicit(builtin, UINT_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, UINT_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, UINT_TO_DOUBLE, DOUBLE));
		members.addCaster(castExplicit(builtin, UINT_TO_CHAR, CHAR));
		members.addCaster(castImplicit(builtin, UINT_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, UINT_PARSE, "parse", UINT, STRING));
		members.addMethod(staticMethod(builtin, UINT_PARSE_WITH_BASE, "parse", UINT, STRING, INT));
		
		members.addMethod(method(builtin, UINT_COUNT_LOW_ZEROES, "countLowZeroes", UINT));
		members.addMethod(method(builtin, UINT_COUNT_HIGH_ZEROES, "countHighZeroes", UINT));
		members.addMethod(method(builtin, UINT_COUNT_LOW_ONES, "countLowOnes", UINT));
		members.addMethod(method(builtin, UINT_COUNT_HIGH_ONES, "countHighOnes", UINT));
		
		ITypeID optionalInt = registry.getOptional(INT);
		members.addGetter(getter(builtin, UINT_HIGHEST_ONE_BIT, "highestOneBit", optionalInt));
		members.addGetter(getter(builtin, UINT_LOWEST_ONE_BIT, "lowestOneBit", optionalInt));
		members.addGetter(getter(builtin, UINT_HIGHEST_ZERO_BIT, "highestZeroBit", optionalInt));
		members.addGetter(getter(builtin, UINT_LOWEST_ZERO_BIT, "lowestZeroBit", optionalInt));
		members.addGetter(getter(builtin, UINT_BIT_COUNT, "bitCount", UINT));
		
		processType(builtin, UINT);
	}
	
	private void visitLong() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "long", Modifiers.EXPORT, null);
		
		members.addOperator(invert(builtin, LONG_NOT, LONG));
		members.addOperator(neg(builtin, LONG_NEG, LONG));
		members.addOperator(inc(builtin, LONG_DEC, LONG));
		members.addOperator(dec(builtin, LONG_INC, LONG));

		members.addOperator(add(builtin, LONG_ADD_LONG, LONG, LONG));
		members.addOperator(add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, LONG_TO_FLOAT));
		members.addOperator(add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addOperator(sub(builtin, LONG_SUB_LONG, LONG, LONG));
		members.addOperator(sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, LONG_TO_FLOAT));
		members.addOperator(sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addOperator(mul(builtin, LONG_MUL_LONG, LONG, LONG));
		members.addOperator(mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, LONG_TO_FLOAT));
		members.addOperator(mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addOperator(div(builtin, LONG_DIV_LONG, LONG, LONG));
		members.addOperator(div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, LONG_TO_FLOAT));
		members.addOperator(div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addOperator(mod(builtin, LONG_MOD_LONG, LONG, LONG));
		
		members.addOperator(or(builtin, LONG_OR_LONG, LONG, LONG));
		members.addOperator(and(builtin, LONG_AND_LONG, LONG, LONG));
		members.addOperator(xor(builtin, LONG_XOR_LONG, LONG, LONG));
		
		members.addOperator(shl(builtin, LONG_SHL, INT, LONG));
		members.addOperator(shr(builtin, LONG_SHR, INT, LONG));
		members.addOperator(ushr(builtin, LONG_USHR, INT, LONG));
		
		members.addOperator(compare(builtin, LONG_COMPARE, LONG));
		members.addOperator(compare(builtin, FLOAT_COMPARE, FLOAT, LONG_TO_FLOAT));
		members.addOperator(compare(builtin, DOUBLE_COMPARE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addConst(constant(builtin, LONG_GET_MIN_VALUE, "MIN_VALUE", new ConstantLongExpression(BUILTIN, Long.MIN_VALUE)));
		members.addConst(constant(builtin, LONG_GET_MAX_VALUE, "MAX_VALUE", new ConstantLongExpression(BUILTIN, Long.MAX_VALUE)));
		
		members.addCaster(castExplicit(builtin, LONG_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, LONG_TO_SBYTE, SBYTE));
		members.addCaster(castExplicit(builtin, LONG_TO_SHORT, SHORT));
		members.addCaster(castExplicit(builtin, LONG_TO_USHORT, USHORT));
		members.addCaster(castExplicit(builtin, LONG_TO_INT, INT));
		members.addCaster(castExplicit(builtin, LONG_TO_UINT, UINT));
		members.addCaster(castImplicit(builtin, LONG_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, LONG_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, LONG_TO_DOUBLE, DOUBLE));
		members.addCaster(castExplicit(builtin, LONG_TO_CHAR, CHAR));
		members.addCaster(castImplicit(builtin, LONG_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, LONG_PARSE, "parse", LONG, STRING));
		members.addMethod(staticMethod(builtin, LONG_PARSE_WITH_BASE, "parse", LONG, STRING, INT));
		
		members.addMethod(method(builtin, LONG_COUNT_LOW_ZEROES, "countLowZeroes", INT));
		members.addMethod(method(builtin, LONG_COUNT_HIGH_ZEROES, "countHighZeroes", INT));
		members.addMethod(method(builtin, LONG_COUNT_LOW_ONES, "countLowOnes", INT));
		members.addMethod(method(builtin, LONG_COUNT_HIGH_ONES, "countHighOnes", INT));
		
		ITypeID optionalInt = registry.getOptional(INT);
		members.addGetter(getter(builtin, LONG_HIGHEST_ONE_BIT, "highestOneBit", optionalInt));
		members.addGetter(getter(builtin, LONG_LOWEST_ONE_BIT, "lowestOneBit", optionalInt));
		members.addGetter(getter(builtin, LONG_HIGHEST_ZERO_BIT, "highestZeroBit", optionalInt));
		members.addGetter(getter(builtin, LONG_LOWEST_ZERO_BIT, "lowestZeroBit", optionalInt));
		members.addGetter(getter(builtin, LONG_BIT_COUNT, "bitCount", INT));
		
		processType(builtin, LONG);
	}
	
	private void visitULong() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "ulong", Modifiers.EXPORT, null);
		
		members.addOperator(invert(builtin, ULONG_NOT, ULONG));
		members.addOperator(inc(builtin, ULONG_DEC, ULONG));
		members.addOperator(dec(builtin, ULONG_INC, ULONG));

		members.addOperator(add(builtin, ULONG_ADD_ULONG, ULONG, ULONG));
		members.addOperator(add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, ULONG_TO_FLOAT));
		members.addOperator(add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, ULONG_TO_DOUBLE));
		
		members.addOperator(sub(builtin, ULONG_SUB_ULONG, ULONG, ULONG));
		members.addOperator(sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, ULONG_TO_FLOAT));
		members.addOperator(sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, ULONG_TO_DOUBLE));
		
		members.addOperator(mul(builtin, ULONG_MUL_ULONG, ULONG, ULONG));
		members.addOperator(mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, ULONG_TO_FLOAT));
		members.addOperator(mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, ULONG_TO_DOUBLE));
		
		members.addOperator(div(builtin, ULONG_DIV_ULONG, ULONG, ULONG));
		members.addOperator(div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, ULONG_TO_FLOAT));
		members.addOperator(div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, ULONG_TO_DOUBLE));
		
		members.addOperator(mod(builtin, ULONG_MOD_ULONG, ULONG, ULONG));
		
		members.addOperator(or(builtin, ULONG_OR_ULONG, ULONG, ULONG));
		members.addOperator(and(builtin, ULONG_AND_ULONG, ULONG, ULONG));
		members.addOperator(xor(builtin, ULONG_XOR_ULONG, ULONG, ULONG));
		
		members.addOperator(shl(builtin, ULONG_SHL, INT, ULONG));
		members.addOperator(shr(builtin, ULONG_SHR, INT, ULONG));
		
		members.addOperator(compare(builtin, ULONG_COMPARE, ULONG));
		members.addOperator(compare(builtin, FLOAT_COMPARE, FLOAT, ULONG_TO_FLOAT));
		members.addOperator(compare(builtin, DOUBLE_COMPARE, DOUBLE, ULONG_TO_DOUBLE));
		
		members.addConst(constant(builtin, ULONG_GET_MIN_VALUE, "MIN_VALUE", new ConstantULongExpression(BUILTIN, 0)));
		members.addConst(constant(builtin, ULONG_GET_MAX_VALUE, "MAX_VALUE", new ConstantULongExpression(BUILTIN, -1L)));
		
		members.addCaster(castExplicit(builtin, ULONG_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, ULONG_TO_SBYTE, SBYTE));
		members.addCaster(castExplicit(builtin, ULONG_TO_SHORT, SHORT));
		members.addCaster(castExplicit(builtin, ULONG_TO_USHORT, USHORT));
		members.addCaster(castExplicit(builtin, ULONG_TO_INT, INT));
		members.addCaster(castExplicit(builtin, ULONG_TO_UINT, UINT));
		members.addCaster(castImplicit(builtin, ULONG_TO_LONG, LONG));
		members.addCaster(castImplicit(builtin, ULONG_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, ULONG_TO_DOUBLE, DOUBLE));
		members.addCaster(castExplicit(builtin, ULONG_TO_CHAR, CHAR));
		members.addCaster(castImplicit(builtin, ULONG_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, ULONG_PARSE, "parse", ULONG, STRING));
		members.addMethod(staticMethod(builtin, ULONG_PARSE_WITH_BASE, "parse", ULONG, STRING, INT));
		
		members.addMethod(method(builtin, ULONG_COUNT_LOW_ZEROES, "countLowZeroes", INT));
		members.addMethod(method(builtin, ULONG_COUNT_HIGH_ZEROES, "countHighZeroes", INT));
		members.addMethod(method(builtin, ULONG_COUNT_LOW_ONES, "countLowOnes", INT));
		members.addMethod(method(builtin, ULONG_COUNT_HIGH_ONES, "countHighOnes", INT));
		
		ITypeID optionalInt = registry.getOptional(INT);
		members.addGetter(getter(builtin, ULONG_HIGHEST_ONE_BIT, "highestOneBit", optionalInt));
		members.addGetter(getter(builtin, ULONG_LOWEST_ONE_BIT, "lowestOneBit", optionalInt));
		members.addGetter(getter(builtin, ULONG_HIGHEST_ZERO_BIT, "highestZeroBit", optionalInt));
		members.addGetter(getter(builtin, ULONG_LOWEST_ZERO_BIT, "lowestZeroBit", optionalInt));
		members.addGetter(getter(builtin, ULONG_BIT_COUNT, "bitCount", INT));
		
		processType(builtin, ULONG);
	}
	
	private void visitFloat() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "float", Modifiers.EXPORT, null);
		
		members.addOperator(neg(builtin, FLOAT_NEG, FLOAT));
		members.addOperator(inc(builtin, FLOAT_DEC, FLOAT));
		members.addOperator(dec(builtin, FLOAT_INC, FLOAT));

		members.addOperator(add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT));
		members.addOperator(add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, FLOAT_TO_DOUBLE));
		
		members.addOperator(sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT));
		members.addOperator(sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addOperator(mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT));
		members.addOperator(mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addOperator(div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT));
		members.addOperator(div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addOperator(compare(builtin, FLOAT_COMPARE, FLOAT));
		members.addOperator(compare(builtin, DOUBLE_COMPARE, DOUBLE, LONG_TO_DOUBLE));
		
		members.addConst(constant(builtin, FLOAT_GET_MIN_VALUE, "MIN_VALUE", new ConstantFloatExpression(BUILTIN, Float.MIN_VALUE)));
		members.addConst(constant(builtin, FLOAT_GET_MAX_VALUE, "MAX_VALUE", new ConstantFloatExpression(BUILTIN, Float.MAX_VALUE)));
		
		members.addCaster(castExplicit(builtin, FLOAT_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, FLOAT_TO_SBYTE, SBYTE));
		members.addCaster(castExplicit(builtin, FLOAT_TO_SHORT, SHORT));
		members.addCaster(castExplicit(builtin, FLOAT_TO_USHORT, USHORT));
		members.addCaster(castExplicit(builtin, FLOAT_TO_INT, INT));
		members.addCaster(castExplicit(builtin, FLOAT_TO_UINT, UINT));
		members.addCaster(castExplicit(builtin, FLOAT_TO_ULONG, ULONG));
		members.addCaster(castExplicit(builtin, FLOAT_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, FLOAT_TO_DOUBLE, DOUBLE));
		members.addCaster(castImplicit(builtin, FLOAT_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, FLOAT_PARSE, "parse", FLOAT, STRING));
		members.addMethod(staticMethod(builtin, FLOAT_FROM_BITS, "fromBits", FLOAT, INT));
		
		members.addGetter(getter(builtin, FLOAT_BITS, "bits", INT));
		
		processType(builtin, FLOAT);
	}
	
	private void visitDouble() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "double", Modifiers.EXPORT, null);
		
		members.addOperator(neg(builtin, DOUBLE_NEG, DOUBLE));
		members.addOperator(inc(builtin, DOUBLE_DEC, DOUBLE));
		members.addOperator(dec(builtin, DOUBLE_INC, DOUBLE));

		members.addOperator(add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE));
		members.addOperator(sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE));
		members.addOperator(mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE));
		members.addOperator(div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE));
		members.addOperator(compare(builtin, DOUBLE_COMPARE, DOUBLE));
		
		members.addConst(constant(builtin, DOUBLE_GET_MIN_VALUE, "MIN_VALUE", new ConstantDoubleExpression(BUILTIN, Double.MIN_VALUE)));
		members.addConst(constant(builtin, DOUBLE_GET_MAX_VALUE, "MAX_VALUE", new ConstantDoubleExpression(BUILTIN, Double.MAX_VALUE)));
		
		members.addCaster(castExplicit(builtin, DOUBLE_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, DOUBLE_TO_SBYTE, SBYTE));
		members.addCaster(castExplicit(builtin, DOUBLE_TO_SHORT, SHORT));
		members.addCaster(castExplicit(builtin, DOUBLE_TO_USHORT, USHORT));
		members.addCaster(castExplicit(builtin, DOUBLE_TO_INT, INT));
		members.addCaster(castExplicit(builtin, DOUBLE_TO_UINT, UINT));
		members.addCaster(castExplicit(builtin, DOUBLE_TO_ULONG, ULONG));
		members.addCaster(castExplicit(builtin, DOUBLE_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, DOUBLE_TO_FLOAT, FLOAT));
		members.addCaster(castImplicit(builtin, DOUBLE_TO_STRING, STRING));
		
		members.addMethod(staticMethod(builtin, DOUBLE_PARSE, "parse", DOUBLE, STRING));
		members.addMethod(staticMethod(builtin, DOUBLE_FROM_BITS, "fromBits", DOUBLE, LONG));
		
		members.addGetter(getter(builtin, DOUBLE_BITS, "bits", LONG));
		
		processType(builtin, DOUBLE);
	}

	private void visitChar() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "char", Modifiers.EXPORT, null);
		
		members.addOperator(add(builtin, CHAR_ADD_INT, INT, CHAR));
		members.addOperator(sub(builtin, CHAR_SUB_INT, INT, CHAR));
		members.addOperator(sub(builtin, CHAR_SUB_CHAR, CHAR, INT));
		members.addOperator(compare(builtin, CHAR_COMPARE, CHAR));
		
		members.addCaster(castExplicit(builtin, CHAR_TO_BYTE, BYTE));
		members.addCaster(castExplicit(builtin, CHAR_TO_SBYTE, SBYTE));
		members.addCaster(castExplicit(builtin, CHAR_TO_SHORT, SHORT));
		members.addCaster(castExplicit(builtin, CHAR_TO_USHORT, USHORT));
		members.addCaster(castImplicit(builtin, CHAR_TO_INT, INT));
		members.addCaster(castImplicit(builtin, CHAR_TO_UINT, UINT));
		members.addCaster(castImplicit(builtin, CHAR_TO_LONG, LONG));
		members.addCaster(castImplicit(builtin, CHAR_TO_ULONG, ULONG));
		members.addCaster(castImplicit(builtin, CHAR_TO_STRING, STRING));
		
		members.addGetter(getter(builtin, CHAR_GET_MIN_VALUE, "MIN_VALUE", CHAR));
		members.addGetter(getter(builtin, CHAR_GET_MAX_VALUE, "MAX_VALUE", CHAR));
		
		members.addMethod(method(builtin, CHAR_REMOVE_DIACRITICS, "removeDiacritics", CHAR));
		members.addMethod(method(builtin, CHAR_TO_LOWER_CASE, "toLowerCase", CHAR));
		members.addMethod(method(builtin, CHAR_TO_UPPER_CASE, "toUpperCase", CHAR));
		
		processType(builtin, CHAR);
	}

	private void visitString() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "string", Modifiers.EXPORT, null);
		
		members.addConstructor(constructor(builtin, STRING_CONSTRUCTOR_CHARACTERS, registry.getArray(CHAR, 1)));
		
		members.addOperator(add(builtin, STRING_ADD_STRING, STRING, STRING));
		members.addOperator(indexGet(builtin, STRING_INDEXGET, INT, CHAR));
		members.addOperator(indexGet(builtin, STRING_RANGEGET, registry.getRange(INT, INT), STRING));
		members.addOperator(compare(builtin, STRING_COMPARE, STRING));
		
		members.addGetter(getter(builtin, STRING_LENGTH, "length", INT));
		members.addGetter(getter(builtin, STRING_CHARACTERS, "characters", registry.getArray(CHAR, 1)));
		members.addGetter(getter(builtin, STRING_ISEMPTY, "isEmpty", BOOL));

		members.addMethod(method(builtin, STRING_REMOVE_DIACRITICS, "removeDiacritics", STRING));
		members.addMethod(method(builtin, STRING_TRIM, "trim", STRING, STRING));
		members.addMethod(method(builtin, STRING_TO_LOWER_CASE, "toLowerCase", STRING));
		members.addMethod(method(builtin, STRING_TO_UPPER_CASE, "toUpperCase", STRING));
		
		members.addIterator(new StringCharIterator(), TypeMemberPriority.SPECIFIED);
		
		processType(builtin, STRING);
	}
	
	private static CallTranslator castedTargetCall(HighLevelDefinition definition, FunctionalMember member, BuiltinID casterBuiltin) {
		CasterMember caster = castImplicit(definition, casterBuiltin, member.header.parameters[0].type);
		return call -> member.call(call.position, caster.cast(call.position, call.target, true), call.arguments, call.scope);
	}
	
	private static OperatorMember not(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.NOT,
				new FunctionHeader(result),
				id);
	}
	
	private static OperatorMember invert(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.INVERT,
				new FunctionHeader(result),
				id);
	}
	
	private static OperatorMember neg(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.NEG,
				new FunctionHeader(result),
				id);
	}
	
	private static OperatorMember inc(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.INCREMENT,
				new FunctionHeader(result),
				id);
	}
	
	private static OperatorMember dec(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.DECREMENT,
				new FunctionHeader(result),
				id);
	}
	
	private static OperatorMember add(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.ADD,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember add(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.ADD,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, add(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember sub(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SUB,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember sub(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.SUB,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, sub(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember mul(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.MUL,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember mul(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.MUL,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, mul(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember div(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.DIV,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember div(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.DIV,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, div(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember mod(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.MOD,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember mod(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.MOD,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, mod(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember shl(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SHL,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember shl(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.SHL,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, shl(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember shr(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC, 
				OperatorType.SHR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember shr(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.SHR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, shr(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember ushr(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.USHR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember ushr(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.USHR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, ushr(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember or(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.OR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember or(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.OR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, or(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember and(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.AND,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember and(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.AND,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, and(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember xor(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.XOR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember xor(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.XOR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				castedTargetCall(definition, xor(definition, id, operand, result), caster),
				null);
	}
	
	private static OperatorMember indexGet(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.INDEXGET,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember indexSet(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID value) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.INDEXSET,
				new FunctionHeader(VOID, operand, value),
				id);
	}
	
	private static OperatorMember compare(HighLevelDefinition cls, BuiltinID id, ITypeID operand) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.COMPARE,
				new FunctionHeader(INT, new FunctionParameter(operand)),
				id);
	}
	
	private static OperatorMember compare(HighLevelDefinition definition, BuiltinID id, ITypeID operand, BuiltinID caster) {
		return new TranslatedOperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.COMPARE,
				new FunctionHeader(BOOL, new FunctionParameter(operand)),
				castedTargetCall(definition, compare(definition, id, operand), caster),
				null);
	}
	
	private static GetterMember getter(HighLevelDefinition cls, BuiltinID id, String name, ITypeID type) {
		return new GetterMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				name,
				type,
				id);
	}
	
	private static ConstMember constant(HighLevelDefinition cls, BuiltinID id, String name, Expression value) {
		ConstMember result = new ConstMember(
				BUILTIN,
				cls,
				Modifiers.STATIC | Modifiers.PUBLIC,
				name,
				value.type,
				id);
		result.value = value;
		return result;
	}
	
	private static ConstructorMember constructor(
			ClassDefinition cls,
			BuiltinID id,
			ITypeID... arguments) {
		return new ConstructorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				new FunctionHeader(VOID, arguments),
				id);
	}
	
	private static MethodMember method(
			ClassDefinition cls,
			BuiltinID id,
			String name,
			ITypeID result,
			ITypeID... arguments) {
		return new MethodMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				name,
				new FunctionHeader(result, arguments),
				id);
	}
	
	private static MethodMember staticMethod(
			ClassDefinition cls,
			BuiltinID id,
			String name,
			ITypeID result,
			ITypeID... arguments) {
		return new MethodMember(
				BUILTIN,
				cls,
				Modifiers.STATIC | Modifiers.PUBLIC,
				name,
				new FunctionHeader(result, arguments),
				id);
	}
	
	private static CasterMember castExplicit(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		return new CasterMember(
				CodePosition.BUILTIN,
				cls,
				Modifiers.PUBLIC,
				result,
				id);
	}
	
	private static CasterMember castImplicit(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		return new CasterMember(
				CodePosition.BUILTIN,
				cls,
				Modifiers.PUBLIC | Modifiers.IMPLICIT,
				result,
				id);
	}
	
	private static OperatorMember equals(HighLevelDefinition cls, BuiltinID id, ITypeID type) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.EQUALS,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id);
	}
	
	private static OperatorMember same(HighLevelDefinition cls, BuiltinID id, ITypeID type) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SAME,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id);
	}
	
	private static OperatorMember notequals(HighLevelDefinition cls, BuiltinID id, ITypeID type) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.NOTEQUALS,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id);
	}
	
	private static OperatorMember notsame(HighLevelDefinition cls, BuiltinID id, ITypeID type) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.NOTSAME,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id);
	}
}
