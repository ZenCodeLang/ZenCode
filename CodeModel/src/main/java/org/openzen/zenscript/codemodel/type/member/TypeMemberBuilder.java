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
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericMapper;
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
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.builtin.ArrayIteratorKeyValues;
import org.openzen.zenscript.codemodel.member.builtin.ArrayIteratorValues;
import org.openzen.zenscript.codemodel.member.builtin.AssocIterator;
import org.openzen.zenscript.codemodel.member.builtin.RangeIterator;
import org.openzen.zenscript.codemodel.member.builtin.StringCharIterator;
import org.openzen.zenscript.codemodel.member.ref.CasterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.member.ref.TranslatedOperatorMemberRef;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import static org.openzen.zenscript.codemodel.type.member.BuiltinID.*;
import static org.openzen.zencode.shared.CodePosition.BUILTIN;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;

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
			if (mapping == null)
				continue;
			
			GenericMapper mapper = new GenericMapper(registry, mapping);
			for (IDefinitionMember member : expansion.members)
				member.registerTo(members, TypeMemberPriority.SPECIFIED, mapper);
		}
		
		if (members.hasOperator(OperatorType.EQUALS)) {
			DefinitionMemberGroup group = members.getOrCreateGroup(OperatorType.EQUALS);
			DefinitionMemberGroup inverse = members.getOrCreateGroup(OperatorType.NOTEQUALS);
			for (TypeMember<FunctionalMemberRef> method : group.getMethodMembers()) {
				if (!inverse.hasMethod(method.member.getHeader())) {
					notequals(definition, BuiltinID.AUTOOP_NOTEQUALS, method.member.getHeader().parameters[0].type);
				}
			}
		}
	}
	
	private Map<TypeParameter, ITypeID> matchType(ITypeID type, ITypeID pattern) {
		Map<TypeParameter, ITypeID> mapping = new HashMap<>();
		if (type.inferTypeParameters(cache, pattern, mapping))
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
		
		operator(
				definition,
				OperatorType.INDEXGET,
				new FunctionHeader(baseType, indexGetParameters),
				ARRAY_INDEXGET);
		
		if (dimension == 1) {
			FunctionHeader sliceHeader = new FunctionHeader(array, new FunctionParameter(cache.getRegistry().getRange(INT, INT), "range"));
			operator(
					definition,
					OperatorType.INDEXGET,
					sliceHeader,
					ARRAY_INDEXGETRANGE);
		}

		FunctionHeader containsHeader = new FunctionHeader(BOOL, new FunctionParameter(baseType, "value"));
		operator(
				definition,
				OperatorType.CONTAINS,
				containsHeader,
				ARRAY_CONTAINS);
		
		if (baseType.hasDefaultValue()) {
			members.addConstructor(new ConstructorMember(
					BUILTIN,
					definition,
					0,
					new FunctionHeader(VOID, indexGetParameters),
					ARRAY_CONSTRUCTOR_SIZED).ref(null));
		}

		FunctionParameter[] initialValueConstructorParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			initialValueConstructorParameters[i] = new FunctionParameter(INT);
		initialValueConstructorParameters[dimension] = new FunctionParameter(baseType);
		FunctionHeader initialValueConstructorHeader = new FunctionHeader(VOID, initialValueConstructorParameters);
		new ConstructorMember(
				BUILTIN,
				definition,
				0,
				initialValueConstructorHeader,
				ARRAY_CONSTRUCTOR_INITIAL_VALUE)
				.registerTo(members, TypeMemberPriority.SPECIFIED, null);
		
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
				ARRAY_CONSTRUCTOR_LAMBDA).ref(null));
		
		{
			TypeParameter mappedConstructorParameter = new TypeParameter(BUILTIN, "T");
			FunctionHeader mappedConstructorHeaderWithoutIndex = new FunctionHeader(baseType, registry.getGeneric(mappedConstructorParameter));
			FunctionHeader mappedConstructorFunctionWithoutIndex = new FunctionHeader(
					new TypeParameter[] { mappedConstructorParameter },
					VOID,
					null,
					new FunctionParameter(registry.getArray(registry.getGeneric(mappedConstructorParameter), dimension), "original"),
					new FunctionParameter(registry.getFunction(mappedConstructorHeaderWithoutIndex), "projection"));
			members.addConstructor(new ConstructorMember(
					BUILTIN,
					definition,
					Modifiers.PUBLIC,
					mappedConstructorFunctionWithoutIndex,
					ARRAY_CONSTRUCTOR_PROJECTED).ref(null));
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
			constructor(definition, ARRAY_CONSTRUCTOR_PROJECTED_INDEXED, mappedConstructorFunctionWithIndex);
		}
		
		FunctionParameter[] indexSetParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			indexSetParameters[i] = new FunctionParameter(INT, null);
		indexSetParameters[dimension] = new FunctionParameter(baseType, null);

		FunctionHeader indexSetHeader = new FunctionHeader(VOID, indexSetParameters);
		operator(definition, OperatorType.INDEXSET, indexSetHeader, ARRAY_INDEXSET);
		
		if (dimension == 1) {
			getter(definition, ARRAY_LENGTH, "length", INT);
		}

		getter(definition, ARRAY_ISEMPTY, "isEmpty", BOOL);
		getter(definition, ARRAY_HASHCODE, "objectHashCode", INT);
		new ArrayIteratorKeyValues(array).registerTo(members, TypeMemberPriority.SPECIFIED, null);
		new ArrayIteratorValues(array).registerTo(members, TypeMemberPriority.SPECIFIED, null);
		
		equals(definition, ARRAY_EQUALS, array);
		notequals(definition, ARRAY_NOTEQUALS, array);
		same(definition, ARRAY_SAME, array);
		notsame(definition, ARRAY_NOTSAME, array);
		
		processType(definition, array);
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		ITypeID keyType = assoc.keyType;
		ITypeID valueType = assoc.valueType;
		
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "", Modifiers.EXPORT);
		
		constructor(builtin, ASSOC_CONSTRUCTOR);

		indexGet(builtin, ASSOC_INDEXGET, keyType, valueType);
		indexSet(builtin, ASSOC_INDEXSET, keyType, valueType);
		
		method(builtin, ASSOC_GETORDEFAULT, "getOrDefault", valueType, keyType, valueType);
		
		operator(
				builtin,
				OperatorType.CONTAINS,
				new FunctionHeader(BOOL, new FunctionParameter(keyType, "key")),
				ASSOC_CONTAINS);
		
		getter(builtin, BuiltinID.ASSOC_SIZE, "size", INT);
		getter(builtin, BuiltinID.ASSOC_ISEMPTY, "isEmpty", BOOL);
		getter(builtin, BuiltinID.ASSOC_KEYS, "keys", cache.getRegistry().getArray(keyType, 1));
		getter(builtin, BuiltinID.ASSOC_VALUES, "values", cache.getRegistry().getArray(valueType, 1));
		getter(builtin, BuiltinID.ASSOC_HASHCODE, "objectHashCode", BasicTypeID.INT);
		
		new AssocIterator(assoc).registerTo(members, TypeMemberPriority.SPECIFIED, null);
		
		equals(builtin, BuiltinID.ASSOC_EQUALS, assoc);
		notequals(builtin, BuiltinID.ASSOC_NOTEQUALS, assoc);
		same(builtin, BuiltinID.ASSOC_SAME, assoc);
		notsame(builtin, BuiltinID.ASSOC_NOTSAME, assoc);
		
		processType(builtin, assoc);
		return null;
	}
	
	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		TypeParameter functionParameter = new TypeParameter(BUILTIN, "T");
		Map<TypeParameter, ITypeID> parameterFilled = Collections.singletonMap(map.key, registry.getGeneric(functionParameter));
		ITypeID valueType = map.value.instance(new GenericMapper(registry, parameterFilled));
		
		FunctionHeader getOptionalHeader = new FunctionHeader(new TypeParameter[] { functionParameter }, registry.getOptional(valueType), null, new FunctionParameter[0]);
		FunctionHeader putHeader = new FunctionHeader(new TypeParameter[] { functionParameter }, BasicTypeID.VOID, null, new FunctionParameter(valueType));
		FunctionHeader containsHeader = new FunctionHeader(new TypeParameter[] { functionParameter }, BasicTypeID.BOOL, null, new FunctionParameter[0]);
		
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "", Modifiers.EXPORT);
		constructor(builtin, GENERICMAP_CONSTRUCTOR);
		
		method(builtin, "getOptional", getOptionalHeader, GENERICMAP_GETOPTIONAL);
		method(builtin, "put", putHeader, GENERICMAP_PUT);
		method(builtin, "contains", containsHeader, GENERICMAP_CONTAINS);
		method(builtin, "addAll", new FunctionHeader(VOID, map), GENERICMAP_ADDALL);
		
		getter(builtin, GENERICMAP_SIZE, "size", INT);
		getter(builtin, GENERICMAP_ISEMPTY, "isEmpty", BOOL);
		getter(builtin, GENERICMAP_HASHCODE, "objectHashCode", INT);
		
		equals(builtin, GENERICMAP_EQUALS, map);
		notequals(builtin, GENERICMAP_NOTEQUALS, map);
		same(builtin, GENERICMAP_SAME, map);
		notsame(builtin, GENERICMAP_NOTSAME, map);
		
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
		new CallerMember(BUILTIN, builtin, 0, function.header, FUNCTION_CALL).registerTo(members, TypeMemberPriority.SPECIFIED, null);
		
		same(builtin, FUNCTION_SAME, function);
		notsame(builtin, FUNCTION_NOTSAME, function);
		
		processType(builtin, function);
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID type) {
		HighLevelDefinition definition = type.definition;
		GenericMapper mapper = null;
		if (type.hasTypeParameters() || (type.outer != null && type.outer.hasTypeParameters())) {
			Map<TypeParameter, ITypeID> mapping = type.getTypeParameterMapping();
			mapper = new GenericMapper(registry, mapping);
		}
		
		for (IDefinitionMember member : definition.members) {
			member.registerTo(members, TypeMemberPriority.SPECIFIED, mapper);
		}

		if (definition instanceof VariantDefinition) {
			VariantDefinition variant = (VariantDefinition) definition;
			for (VariantDefinition.Option option : variant.options)
				members.addVariantOption(option.instance(type, mapper));
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
				constructor(definition, CLASS_DEFAULT_CONSTRUCTOR);
			} else if (definition instanceof StructDefinition) {
				// add default struct constructors (TODO: only works if all fields have a default value)
				constructor(definition, STRUCT_EMPTY_CONSTRUCTOR);
				
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
							STRUCT_VALUE_CONSTRUCTOR).ref(null), TypeMemberPriority.SPECIFIED);
				}
			} else if (definition instanceof EnumDefinition) {
				// add default constructor
				constructors.addMethod(new ConstructorMember(
						BUILTIN,
						definition,
						Modifiers.PRIVATE,
						new FunctionHeader(VOID),
						ENUM_EMPTY_CONSTRUCTOR).ref(null), TypeMemberPriority.SPECIFIED);
			}
		}
		
		if (definition instanceof EnumDefinition) {
			getter(definition, ENUM_NAME, "name", STRING);
			getter(definition, ENUM_ORDINAL, "ordinal", INT);
			
			List<EnumConstantMember> enumConstants = ((EnumDefinition) definition).enumConstants;
			Expression[] constValues = new Expression[enumConstants.size()];
			for (int i = 0; i < constValues.length; i++)
				constValues[i] = new EnumConstantExpression(BUILTIN, type, enumConstants.get(i));
			
			constant(definition, ENUM_VALUES, "values", new ArrayExpression(BUILTIN, constValues, registry.getArray(type, 1)));
			compare(definition, ENUM_COMPARE, type);
			
			if (!members.canCast(BasicTypeID.STRING)) {
				castImplicit(definition, ENUM_TO_STRING, STRING);
			}
		}
		
		if (definition instanceof InterfaceDefinition) {
			InterfaceDefinition interfaceDefinition = (InterfaceDefinition)definition;
			for (ITypeID baseType : interfaceDefinition.baseInterfaces)
				cache.get(baseType.instance(mapper)).copyMembersTo(type.definition.position, members, TypeMemberPriority.INHERITED);
		}
		
		if (type.superType != null) {
			cache.get(type.superType.instance(mapper)).copyMembersTo(type.definition.position, members, TypeMemberPriority.INHERITED);
		} else {
			getter(definition, OBJECT_HASHCODE, "objectHashCode", BasicTypeID.INT);
		}
		
		same(definition, OBJECT_SAME, type);
		notsame(definition, OBJECT_NOTSAME, type);
		
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
		getter(definition, RANGE_FROM, "from", fromType);
		getter(definition, RANGE_TO, "to", toType);
		if (range.from == range.to && (range.from == BasicTypeID.BYTE
				|| range.from == SBYTE
				|| range.from == SHORT
				|| range.from == USHORT
				|| range.from == INT
				|| range.from == UINT
				|| range.from == LONG
				|| range.from == ULONG)) {
			new RangeIterator(range).registerTo(members, TypeMemberPriority.SPECIFIED, null);
		}
		
		processType(definition, range);
		return null;
	}

	@Override
	public Void visitModified(ModifiedTypeID modified) {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "modified", Modifiers.EXPORT, null);
		modified.baseType.accept(this);
		
		if (modified.isOptional()) {
			operator(builtin, OperatorType.EQUALS, new FunctionHeader(BOOL, NULL), BuiltinID.OPTIONAL_IS_NULL);
			operator(builtin, OperatorType.NOTEQUALS, new FunctionHeader(BOOL, NULL), BuiltinID.OPTIONAL_IS_NOT_NULL);
		}
		processType(builtin, modified);
		return null;
	}
	
	private void visitBool() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "bool", Modifiers.EXPORT, null);
		not(builtin, BOOL_NOT, BOOL);
		and(builtin, BOOL_AND, BOOL, BOOL);
		or(builtin, BOOL_OR, BOOL, BOOL);
		xor(builtin, BOOL_XOR, BOOL, BOOL);
		equals(builtin, BOOL_EQUALS, BOOL);
		notequals(builtin, BOOL_NOTEQUALS, BOOL);
		
		castExplicit(builtin, BOOL_TO_STRING, STRING);
		staticMethod(builtin, BOOL_PARSE, "parse", BOOL, STRING);
		
		processType(builtin, BOOL);
	}
	
	private void visitByte() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "byte", Modifiers.EXPORT, null);
		
		invert(builtin, BYTE_NOT, BYTE);
		inc(builtin, BYTE_INC, BYTE);
		dec(builtin, BYTE_DEC, BYTE);
		add(builtin, BYTE_ADD_BYTE, BYTE, BYTE);
		sub(builtin, BYTE_SUB_BYTE, BYTE, BYTE);
		mul(builtin, BYTE_MUL_BYTE, BYTE, BYTE);
		div(builtin, BYTE_DIV_BYTE, BYTE, BYTE);
		mod(builtin, BYTE_MOD_BYTE, BYTE, BYTE);
		and(builtin, BYTE_AND_BYTE, BYTE, BYTE);
		or(builtin, BYTE_OR_BYTE, BYTE, BYTE);
		xor(builtin, BYTE_XOR_BYTE, BYTE, BYTE);
		compare(builtin, BYTE_COMPARE, BYTE);
		
		castImplicit(builtin, BYTE_TO_SBYTE, SBYTE);
		castImplicit(builtin, BYTE_TO_SHORT, SHORT);
		castImplicit(builtin, BYTE_TO_USHORT, USHORT);
		castImplicit(builtin, BYTE_TO_INT, INT);
		castImplicit(builtin, BYTE_TO_UINT, UINT);
		castImplicit(builtin, BYTE_TO_LONG, LONG);
		castImplicit(builtin, BYTE_TO_ULONG, ULONG);
		castImplicit(builtin, BYTE_TO_FLOAT, FLOAT);
		castImplicit(builtin, BYTE_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, BYTE_TO_CHAR, CHAR);
		castImplicit(builtin, BYTE_TO_STRING, STRING);
		
		staticMethod(builtin, BYTE_PARSE, "parse", BYTE, STRING);
		staticMethod(builtin, BYTE_PARSE_WITH_BASE, "parse", BYTE, STRING, INT);
		
		constant(builtin, BYTE_GET_MIN_VALUE, "MIN_VALUE", new ConstantByteExpression(BUILTIN, 0));
		constant(builtin, BYTE_GET_MAX_VALUE, "MAX_VALUE", new ConstantByteExpression(BUILTIN, 255));
		
		processType(builtin, BYTE);
	}
	
	private void visitSByte() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "sbyte", Modifiers.EXPORT, null);
		
		invert(builtin, SBYTE_NOT, SBYTE);
		neg(builtin, SBYTE_NEG, SBYTE);
		inc(builtin, SBYTE_INC, SBYTE);
		dec(builtin, SBYTE_DEC, SBYTE);
		add(builtin, SBYTE_ADD_SBYTE, SBYTE, SBYTE);
		sub(builtin, SBYTE_SUB_SBYTE, SBYTE, SBYTE);
		mul(builtin, SBYTE_MUL_SBYTE, SBYTE, SBYTE);
		div(builtin, SBYTE_DIV_SBYTE, SBYTE, SBYTE);
		mod(builtin, SBYTE_MOD_SBYTE, SBYTE, SBYTE);
		and(builtin, SBYTE_AND_SBYTE, SBYTE, SBYTE);
		or(builtin, SBYTE_OR_SBYTE, SBYTE, SBYTE);
		xor(builtin, SBYTE_XOR_SBYTE, SBYTE, SBYTE);
		compare(builtin, SBYTE_COMPARE, SBYTE);
		
		castImplicit(builtin, SBYTE_TO_BYTE, BYTE);
		castImplicit(builtin, SBYTE_TO_SHORT, SHORT);
		castImplicit(builtin, SBYTE_TO_USHORT, USHORT);
		castImplicit(builtin, SBYTE_TO_INT, INT);
		castImplicit(builtin, SBYTE_TO_UINT, UINT);
		castImplicit(builtin, SBYTE_TO_LONG, LONG);
		castImplicit(builtin, SBYTE_TO_ULONG, ULONG);
		castImplicit(builtin, SBYTE_TO_FLOAT, FLOAT);
		castImplicit(builtin, SBYTE_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, SBYTE_TO_CHAR, CHAR);
		castImplicit(builtin, SBYTE_TO_STRING, STRING);
		
		staticMethod(builtin, SBYTE_PARSE, "parse", SBYTE, STRING);
		staticMethod(builtin, SBYTE_PARSE_WITH_BASE, "parse", SBYTE, STRING, INT);
		
		constant(builtin, SBYTE_GET_MIN_VALUE, "MIN_VALUE", new ConstantSByteExpression(BUILTIN, Byte.MIN_VALUE));
		constant(builtin, SBYTE_GET_MAX_VALUE, "MAX_VALUE", new ConstantSByteExpression(BUILTIN, Byte.MAX_VALUE));
		
		processType(builtin, SBYTE);
	}
	
	private void visitShort() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "short", Modifiers.EXPORT, null);
		
		invert(builtin, SHORT_NOT, SHORT);
		neg(builtin, SHORT_NEG, SHORT);
		inc(builtin, SHORT_INC, SHORT);
		dec(builtin, SHORT_DEC, SHORT);
		add(builtin, SHORT_ADD_SHORT, SHORT, SHORT);
		sub(builtin, SHORT_SUB_SHORT, SHORT, SHORT);
		mul(builtin, SHORT_MUL_SHORT, SHORT, SHORT);
		div(builtin, SHORT_DIV_SHORT, SHORT, SHORT);
		mod(builtin, SHORT_MOD_SHORT, SHORT, SHORT);
		and(builtin, SHORT_AND_SHORT, SHORT, SHORT);
		or(builtin, SHORT_OR_SHORT, SHORT, SHORT);
		xor(builtin, SHORT_XOR_SHORT, SHORT, SHORT);
		compare(builtin, SHORT_COMPARE, SHORT);
		
		castExplicit(builtin, SHORT_TO_BYTE, BYTE);
		castExplicit(builtin, SHORT_TO_SBYTE, SBYTE);
		castImplicit(builtin, SHORT_TO_USHORT, USHORT);
		castImplicit(builtin, SHORT_TO_INT, INT);
		castImplicit(builtin, SHORT_TO_UINT, UINT);
		castImplicit(builtin, SHORT_TO_LONG, LONG);
		castImplicit(builtin, SHORT_TO_ULONG, ULONG);
		castImplicit(builtin, SHORT_TO_FLOAT, FLOAT);
		castImplicit(builtin, SHORT_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, SHORT_TO_CHAR, CHAR);
		castImplicit(builtin, SHORT_TO_STRING, STRING);
		
		staticMethod(builtin, SHORT_PARSE, "parse", SHORT, STRING);
		staticMethod(builtin, SHORT_PARSE_WITH_BASE, "parse", SHORT, STRING, INT);
		
		constant(builtin, SHORT_GET_MIN_VALUE, "MIN_VALUE", new ConstantShortExpression(BUILTIN, Short.MIN_VALUE));
		constant(builtin, SHORT_GET_MAX_VALUE, "MAX_VALUE", new ConstantShortExpression(BUILTIN, Short.MAX_VALUE));
		
		processType(builtin, SHORT);
	}
	
	private void visitUShort() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "ushort", Modifiers.EXPORT, null);
		
		invert(builtin, USHORT_NOT, USHORT);
		inc(builtin, USHORT_INC, USHORT);
		dec(builtin, USHORT_DEC, USHORT);
		add(builtin, USHORT_ADD_USHORT, USHORT, USHORT);
		sub(builtin, USHORT_SUB_USHORT, USHORT, USHORT);
		mul(builtin, USHORT_MUL_USHORT, USHORT, USHORT);
		div(builtin, USHORT_DIV_USHORT, USHORT, USHORT);
		mod(builtin, USHORT_MOD_USHORT, USHORT, USHORT);
		and(builtin, USHORT_AND_USHORT, USHORT, USHORT);
		or(builtin, USHORT_OR_USHORT, USHORT, USHORT);
		xor(builtin, USHORT_XOR_USHORT, USHORT, USHORT);
		compare(builtin, USHORT_COMPARE, USHORT);
		
		castExplicit(builtin, USHORT_TO_BYTE, BYTE);
		castExplicit(builtin, USHORT_TO_SBYTE, SBYTE);
		castImplicit(builtin, USHORT_TO_SHORT, SHORT);
		castImplicit(builtin, USHORT_TO_INT, INT);
		castImplicit(builtin, USHORT_TO_UINT, UINT);
		castImplicit(builtin, USHORT_TO_LONG, LONG);
		castImplicit(builtin, USHORT_TO_ULONG, ULONG);
		castImplicit(builtin, USHORT_TO_FLOAT, FLOAT);
		castImplicit(builtin, USHORT_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, USHORT_TO_CHAR, CHAR);
		castImplicit(builtin, USHORT_TO_STRING, STRING);
		
		staticMethod(builtin, USHORT_PARSE, "parse", USHORT, STRING);
		staticMethod(builtin, USHORT_PARSE_WITH_BASE, "parse", USHORT, STRING, INT);
		
		constant(builtin, USHORT_GET_MIN_VALUE, "MIN_VALUE", new ConstantUShortExpression(BUILTIN, 0));
		constant(builtin, USHORT_GET_MAX_VALUE, "MAX_VALUE", new ConstantUShortExpression(BUILTIN, 65535));
		
		processType(builtin, USHORT);
	}
	
	private void visitInt() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "int", Modifiers.EXPORT, null);
		
		invert(builtin, INT_NOT, INT);
		neg(builtin, INT_NEG, INT);
		inc(builtin, INT_DEC, INT);
		dec(builtin, INT_INC, INT);

		add(builtin, INT_ADD_INT, INT, INT);
		add(builtin, LONG_ADD_LONG, LONG, LONG, INT_TO_LONG);
		add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, INT_TO_FLOAT);
		add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, INT_TO_DOUBLE);
		
		sub(builtin, INT_SUB_INT, INT, INT);
		sub(builtin, LONG_SUB_LONG, LONG, LONG, INT_TO_LONG);
		sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, INT_TO_FLOAT);
		sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, INT_TO_DOUBLE);
		
		mul(builtin, INT_MUL_INT, INT, INT);
		mul(builtin, LONG_MUL_LONG, LONG, LONG, INT_TO_LONG);
		mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, INT_TO_FLOAT);
		mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, INT_TO_DOUBLE);
		
		div(builtin, INT_DIV_INT, INT, INT);
		div(builtin, LONG_DIV_LONG, LONG, LONG, INT_TO_LONG);
		div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, INT_TO_FLOAT);
		div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, INT_TO_DOUBLE);
		
		mod(builtin, INT_MOD_INT, INT, INT);
		mod(builtin, LONG_MOD_LONG, LONG, LONG, INT_TO_LONG);
		
		or(builtin, INT_OR_INT, INT, INT);
		or(builtin, LONG_OR_LONG, LONG, LONG, INT_TO_LONG);
		and(builtin, INT_AND_INT, INT, INT);
		and(builtin, LONG_AND_LONG, LONG, LONG, INT_TO_LONG);
		xor(builtin, INT_XOR_INT, INT, INT);
		xor(builtin, LONG_XOR_LONG, LONG, LONG, INT_TO_LONG);
		
		shl(builtin, INT_SHL, INT, INT);
		shr(builtin, INT_SHR, INT, INT);
		ushr(builtin, INT_USHR, INT, INT);
		
		compare(builtin, INT_COMPARE, INT);
		compare(builtin, LONG_COMPARE, LONG, INT_TO_LONG);
		compare(builtin, FLOAT_COMPARE, FLOAT, INT_TO_FLOAT);
		compare(builtin, DOUBLE_COMPARE, DOUBLE, INT_TO_DOUBLE);
		
		constant(builtin, INT_GET_MIN_VALUE, "MIN_VALUE", new ConstantIntExpression(BUILTIN, Integer.MIN_VALUE));
		constant(builtin, INT_GET_MAX_VALUE, "MAX_VALUE", new ConstantIntExpression(BUILTIN, Integer.MAX_VALUE));
		
		castExplicit(builtin, INT_TO_BYTE, BYTE);
		castExplicit(builtin, INT_TO_SBYTE, SBYTE);
		castExplicit(builtin, INT_TO_SHORT, SHORT);
		castExplicit(builtin, INT_TO_USHORT, USHORT);
		castImplicit(builtin, INT_TO_UINT, UINT);
		castImplicit(builtin, INT_TO_LONG, LONG);
		castImplicit(builtin, INT_TO_ULONG, ULONG);
		castImplicit(builtin, INT_TO_FLOAT, FLOAT);
		castImplicit(builtin, INT_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, INT_TO_CHAR, CHAR);
		castImplicit(builtin, INT_TO_STRING, STRING);
		
		staticMethod(builtin, INT_PARSE, "parse", INT, STRING);
		staticMethod(builtin, INT_PARSE_WITH_BASE, "parse", INT, STRING, INT);
		
		method(builtin, INT_COUNT_LOW_ZEROES, "countLowZeroes", INT);
		method(builtin, INT_COUNT_HIGH_ZEROES, "countHighZeroes", INT);
		method(builtin, INT_COUNT_LOW_ONES, "countLowOnes", INT);
		method(builtin, INT_COUNT_HIGH_ONES, "countHighOnes", INT);
		
		ITypeID optionalInt = registry.getOptional(INT);
		getter(builtin, INT_HIGHEST_ONE_BIT, "highestOneBit", optionalInt);
		getter(builtin, INT_LOWEST_ONE_BIT, "lowestOneBit", optionalInt);
		getter(builtin, INT_HIGHEST_ZERO_BIT, "highestZeroBit", optionalInt);
		getter(builtin, INT_LOWEST_ZERO_BIT, "lowestZeroBit", optionalInt);
		getter(builtin, INT_BIT_COUNT, "bitCount", INT);
		
		processType(builtin, INT);
	}

	private void visitUInt() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "uint", Modifiers.EXPORT, null);
		
		invert(builtin, UINT_NOT, INT);
		inc(builtin, UINT_DEC, INT);
		dec(builtin, UINT_INC, INT);

		add(builtin, UINT_ADD_UINT, UINT, UINT);
		add(builtin, ULONG_ADD_ULONG, ULONG, ULONG, UINT_TO_ULONG);
		add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, UINT_TO_FLOAT);
		add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, UINT_TO_DOUBLE);
		
		sub(builtin, UINT_SUB_UINT, UINT, UINT);
		sub(builtin, ULONG_SUB_ULONG, ULONG, ULONG, UINT_TO_ULONG);
		sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, UINT_TO_FLOAT);
		sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, UINT_TO_DOUBLE);
		
		mul(builtin, UINT_MUL_UINT, UINT, UINT);
		mul(builtin, ULONG_MUL_ULONG, ULONG, ULONG, UINT_TO_ULONG);
		mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, UINT_TO_FLOAT);
		mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, UINT_TO_DOUBLE);
		
		div(builtin, UINT_DIV_UINT, UINT, UINT);
		div(builtin, ULONG_DIV_ULONG, ULONG, ULONG, UINT_TO_ULONG);
		div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, UINT_TO_FLOAT);
		div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, UINT_TO_DOUBLE);
		
		mod(builtin, UINT_MOD_UINT, UINT, UINT);
		mod(builtin, ULONG_MOD_ULONG, ULONG, ULONG, UINT_TO_ULONG);
		
		or(builtin, UINT_OR_UINT, UINT, UINT);
		or(builtin, ULONG_OR_ULONG, ULONG, ULONG, UINT_TO_ULONG);
		and(builtin, UINT_AND_UINT, UINT, UINT);
		and(builtin, ULONG_AND_ULONG, ULONG, ULONG, UINT_TO_ULONG);
		xor(builtin, UINT_XOR_UINT, UINT, UINT);
		xor(builtin, ULONG_XOR_ULONG, ULONG, ULONG, UINT_TO_ULONG);
		
		shl(builtin, UINT_SHL, UINT, UINT);
		shr(builtin, UINT_SHR, UINT, UINT);
		
		compare(builtin, UINT_COMPARE, UINT);
		compare(builtin, ULONG_COMPARE, ULONG, UINT_TO_LONG);
		compare(builtin, FLOAT_COMPARE, FLOAT, UINT_TO_FLOAT);
		compare(builtin, DOUBLE_COMPARE, DOUBLE, UINT_TO_DOUBLE);
		
		constant(builtin, UINT_GET_MIN_VALUE, "MIN_VALUE", new ConstantUIntExpression(BUILTIN, 0));
		constant(builtin, UINT_GET_MAX_VALUE, "MAX_VALUE", new ConstantUIntExpression(BUILTIN, -1));
		
		castExplicit(builtin, UINT_TO_BYTE, BYTE);
		castExplicit(builtin, UINT_TO_SBYTE, SBYTE);
		castExplicit(builtin, UINT_TO_SHORT, SHORT);
		castExplicit(builtin, UINT_TO_USHORT, USHORT);
		castImplicit(builtin, UINT_TO_INT, INT);
		castImplicit(builtin, UINT_TO_LONG, LONG);
		castImplicit(builtin, UINT_TO_ULONG, ULONG);
		castImplicit(builtin, UINT_TO_FLOAT, FLOAT);
		castImplicit(builtin, UINT_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, UINT_TO_CHAR, CHAR);
		castImplicit(builtin, UINT_TO_STRING, STRING);
		
		staticMethod(builtin, UINT_PARSE, "parse", UINT, STRING);
		staticMethod(builtin, UINT_PARSE_WITH_BASE, "parse", UINT, STRING, INT);
		
		method(builtin, UINT_COUNT_LOW_ZEROES, "countLowZeroes", UINT);
		method(builtin, UINT_COUNT_HIGH_ZEROES, "countHighZeroes", UINT);
		method(builtin, UINT_COUNT_LOW_ONES, "countLowOnes", UINT);
		method(builtin, UINT_COUNT_HIGH_ONES, "countHighOnes", UINT);
		
		ITypeID optionalInt = registry.getOptional(INT);
		getter(builtin, UINT_HIGHEST_ONE_BIT, "highestOneBit", optionalInt);
		getter(builtin, UINT_LOWEST_ONE_BIT, "lowestOneBit", optionalInt);
		getter(builtin, UINT_HIGHEST_ZERO_BIT, "highestZeroBit", optionalInt);
		getter(builtin, UINT_LOWEST_ZERO_BIT, "lowestZeroBit", optionalInt);
		getter(builtin, UINT_BIT_COUNT, "bitCount", UINT);
		
		processType(builtin, UINT);
	}
	
	private void visitLong() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "long", Modifiers.EXPORT, null);
		
		invert(builtin, LONG_NOT, LONG);
		neg(builtin, LONG_NEG, LONG);
		inc(builtin, LONG_DEC, LONG);
		dec(builtin, LONG_INC, LONG);

		add(builtin, LONG_ADD_LONG, LONG, LONG);
		add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, LONG_TO_FLOAT);
		add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);
		
		sub(builtin, LONG_SUB_LONG, LONG, LONG);
		sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, LONG_TO_FLOAT);
		sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);
		
		mul(builtin, LONG_MUL_LONG, LONG, LONG);
		mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, LONG_TO_FLOAT);
		mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);
		
		div(builtin, LONG_DIV_LONG, LONG, LONG);
		div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, LONG_TO_FLOAT);
		div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);
		
		mod(builtin, LONG_MOD_LONG, LONG, LONG);
		
		or(builtin, LONG_OR_LONG, LONG, LONG);
		and(builtin, LONG_AND_LONG, LONG, LONG);
		xor(builtin, LONG_XOR_LONG, LONG, LONG);
		
		shl(builtin, LONG_SHL, INT, LONG);
		shr(builtin, LONG_SHR, INT, LONG);
		ushr(builtin, LONG_USHR, INT, LONG);
		
		compare(builtin, LONG_COMPARE, LONG);
		compare(builtin, FLOAT_COMPARE, FLOAT, LONG_TO_FLOAT);
		compare(builtin, DOUBLE_COMPARE, DOUBLE, LONG_TO_DOUBLE);
		
		constant(builtin, LONG_GET_MIN_VALUE, "MIN_VALUE", new ConstantLongExpression(BUILTIN, Long.MIN_VALUE));
		constant(builtin, LONG_GET_MAX_VALUE, "MAX_VALUE", new ConstantLongExpression(BUILTIN, Long.MAX_VALUE));
		
		castExplicit(builtin, LONG_TO_BYTE, BYTE);
		castExplicit(builtin, LONG_TO_SBYTE, SBYTE);
		castExplicit(builtin, LONG_TO_SHORT, SHORT);
		castExplicit(builtin, LONG_TO_USHORT, USHORT);
		castExplicit(builtin, LONG_TO_INT, INT);
		castExplicit(builtin, LONG_TO_UINT, UINT);
		castImplicit(builtin, LONG_TO_ULONG, ULONG);
		castImplicit(builtin, LONG_TO_FLOAT, FLOAT);
		castImplicit(builtin, LONG_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, LONG_TO_CHAR, CHAR);
		castImplicit(builtin, LONG_TO_STRING, STRING);
		
		staticMethod(builtin, LONG_PARSE, "parse", LONG, STRING);
		staticMethod(builtin, LONG_PARSE_WITH_BASE, "parse", LONG, STRING, INT);
		
		method(builtin, LONG_COUNT_LOW_ZEROES, "countLowZeroes", INT);
		method(builtin, LONG_COUNT_HIGH_ZEROES, "countHighZeroes", INT);
		method(builtin, LONG_COUNT_LOW_ONES, "countLowOnes", INT);
		method(builtin, LONG_COUNT_HIGH_ONES, "countHighOnes", INT);
		
		ITypeID optionalInt = registry.getOptional(INT);
		getter(builtin, LONG_HIGHEST_ONE_BIT, "highestOneBit", optionalInt);
		getter(builtin, LONG_LOWEST_ONE_BIT, "lowestOneBit", optionalInt);
		getter(builtin, LONG_HIGHEST_ZERO_BIT, "highestZeroBit", optionalInt);
		getter(builtin, LONG_LOWEST_ZERO_BIT, "lowestZeroBit", optionalInt);
		getter(builtin, LONG_BIT_COUNT, "bitCount", INT);
		
		processType(builtin, LONG);
	}
	
	private void visitULong() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "ulong", Modifiers.EXPORT, null);
		
		invert(builtin, ULONG_NOT, ULONG);
		inc(builtin, ULONG_DEC, ULONG);
		dec(builtin, ULONG_INC, ULONG);

		add(builtin, ULONG_ADD_ULONG, ULONG, ULONG);
		add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, ULONG_TO_FLOAT);
		add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, ULONG_TO_DOUBLE);
		
		sub(builtin, ULONG_SUB_ULONG, ULONG, ULONG);
		sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, ULONG_TO_FLOAT);
		sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, ULONG_TO_DOUBLE);
		
		mul(builtin, ULONG_MUL_ULONG, ULONG, ULONG);
		mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, ULONG_TO_FLOAT);
		mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, ULONG_TO_DOUBLE);
		
		div(builtin, ULONG_DIV_ULONG, ULONG, ULONG);
		div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, ULONG_TO_FLOAT);
		div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, ULONG_TO_DOUBLE);
		
		mod(builtin, ULONG_MOD_ULONG, ULONG, ULONG);
		
		or(builtin, ULONG_OR_ULONG, ULONG, ULONG);
		and(builtin, ULONG_AND_ULONG, ULONG, ULONG);
		xor(builtin, ULONG_XOR_ULONG, ULONG, ULONG);
		
		shl(builtin, ULONG_SHL, INT, ULONG);
		shr(builtin, ULONG_SHR, INT, ULONG);
		
		compare(builtin, ULONG_COMPARE, ULONG);
		compare(builtin, FLOAT_COMPARE, FLOAT, ULONG_TO_FLOAT);
		compare(builtin, DOUBLE_COMPARE, DOUBLE, ULONG_TO_DOUBLE);
		
		constant(builtin, ULONG_GET_MIN_VALUE, "MIN_VALUE", new ConstantULongExpression(BUILTIN, 0));
		constant(builtin, ULONG_GET_MAX_VALUE, "MAX_VALUE", new ConstantULongExpression(BUILTIN, -1L));
		
		castExplicit(builtin, ULONG_TO_BYTE, BYTE);
		castExplicit(builtin, ULONG_TO_SBYTE, SBYTE);
		castExplicit(builtin, ULONG_TO_SHORT, SHORT);
		castExplicit(builtin, ULONG_TO_USHORT, USHORT);
		castExplicit(builtin, ULONG_TO_INT, INT);
		castExplicit(builtin, ULONG_TO_UINT, UINT);
		castImplicit(builtin, ULONG_TO_LONG, LONG);
		castImplicit(builtin, ULONG_TO_FLOAT, FLOAT);
		castImplicit(builtin, ULONG_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, ULONG_TO_CHAR, CHAR);
		castImplicit(builtin, ULONG_TO_STRING, STRING);
		
		staticMethod(builtin, ULONG_PARSE, "parse", ULONG, STRING);
		staticMethod(builtin, ULONG_PARSE_WITH_BASE, "parse", ULONG, STRING, INT);
		
		method(builtin, ULONG_COUNT_LOW_ZEROES, "countLowZeroes", INT);
		method(builtin, ULONG_COUNT_HIGH_ZEROES, "countHighZeroes", INT);
		method(builtin, ULONG_COUNT_LOW_ONES, "countLowOnes", INT);
		method(builtin, ULONG_COUNT_HIGH_ONES, "countHighOnes", INT);
		
		ITypeID optionalInt = registry.getOptional(INT);
		getter(builtin, ULONG_HIGHEST_ONE_BIT, "highestOneBit", optionalInt);
		getter(builtin, ULONG_LOWEST_ONE_BIT, "lowestOneBit", optionalInt);
		getter(builtin, ULONG_HIGHEST_ZERO_BIT, "highestZeroBit", optionalInt);
		getter(builtin, ULONG_LOWEST_ZERO_BIT, "lowestZeroBit", optionalInt);
		getter(builtin, ULONG_BIT_COUNT, "bitCount", INT);
		
		processType(builtin, ULONG);
	}
	
	private void visitFloat() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "float", Modifiers.EXPORT, null);
		
		neg(builtin, FLOAT_NEG, FLOAT);
		inc(builtin, FLOAT_DEC, FLOAT);
		dec(builtin, FLOAT_INC, FLOAT);

		add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT);
		add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, FLOAT_TO_DOUBLE);
		
		sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT);
		sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);
		
		mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT);
		mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);
		
		div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT);
		div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);
		
		compare(builtin, FLOAT_COMPARE, FLOAT);
		compare(builtin, DOUBLE_COMPARE, DOUBLE, LONG_TO_DOUBLE);
		
		constant(builtin, FLOAT_GET_MIN_VALUE, "MIN_VALUE", new ConstantFloatExpression(BUILTIN, Float.MIN_VALUE));
		constant(builtin, FLOAT_GET_MAX_VALUE, "MAX_VALUE", new ConstantFloatExpression(BUILTIN, Float.MAX_VALUE));
		
		castExplicit(builtin, FLOAT_TO_BYTE, BYTE);
		castExplicit(builtin, FLOAT_TO_SBYTE, SBYTE);
		castExplicit(builtin, FLOAT_TO_SHORT, SHORT);
		castExplicit(builtin, FLOAT_TO_USHORT, USHORT);
		castExplicit(builtin, FLOAT_TO_INT, INT);
		castExplicit(builtin, FLOAT_TO_UINT, UINT);
		castExplicit(builtin, FLOAT_TO_ULONG, ULONG);
		castExplicit(builtin, FLOAT_TO_ULONG, ULONG);
		castImplicit(builtin, FLOAT_TO_DOUBLE, DOUBLE);
		castImplicit(builtin, FLOAT_TO_STRING, STRING);
		
		staticMethod(builtin, FLOAT_PARSE, "parse", FLOAT, STRING);
		staticMethod(builtin, FLOAT_FROM_BITS, "fromBits", FLOAT, INT);
		
		getter(builtin, FLOAT_BITS, "bits", INT);
		
		processType(builtin, FLOAT);
	}
	
	private void visitDouble() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "double", Modifiers.EXPORT, null);
		
		neg(builtin, DOUBLE_NEG, DOUBLE);
		inc(builtin, DOUBLE_DEC, DOUBLE);
		dec(builtin, DOUBLE_INC, DOUBLE);

		add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE);
		sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE);
		mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE);
		div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE);
		compare(builtin, DOUBLE_COMPARE, DOUBLE);
		
		constant(builtin, DOUBLE_GET_MIN_VALUE, "MIN_VALUE", new ConstantDoubleExpression(BUILTIN, Double.MIN_VALUE));
		constant(builtin, DOUBLE_GET_MAX_VALUE, "MAX_VALUE", new ConstantDoubleExpression(BUILTIN, Double.MAX_VALUE));
		
		castExplicit(builtin, DOUBLE_TO_BYTE, BYTE);
		castExplicit(builtin, DOUBLE_TO_SBYTE, SBYTE);
		castExplicit(builtin, DOUBLE_TO_SHORT, SHORT);
		castExplicit(builtin, DOUBLE_TO_USHORT, USHORT);
		castExplicit(builtin, DOUBLE_TO_INT, INT);
		castExplicit(builtin, DOUBLE_TO_UINT, UINT);
		castExplicit(builtin, DOUBLE_TO_ULONG, ULONG);
		castExplicit(builtin, DOUBLE_TO_ULONG, ULONG);
		castImplicit(builtin, DOUBLE_TO_FLOAT, FLOAT);
		castImplicit(builtin, DOUBLE_TO_STRING, STRING);
		
		staticMethod(builtin, DOUBLE_PARSE, "parse", DOUBLE, STRING);
		staticMethod(builtin, DOUBLE_FROM_BITS, "fromBits", DOUBLE, LONG);
		
		getter(builtin, DOUBLE_BITS, "bits", LONG);
		
		processType(builtin, DOUBLE);
	}

	private void visitChar() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "char", Modifiers.EXPORT, null);
		
		add(builtin, CHAR_ADD_INT, INT, CHAR);
		sub(builtin, CHAR_SUB_INT, INT, CHAR);
		sub(builtin, CHAR_SUB_CHAR, CHAR, INT);
		compare(builtin, CHAR_COMPARE, CHAR);
		
		castExplicit(builtin, CHAR_TO_BYTE, BYTE);
		castExplicit(builtin, CHAR_TO_SBYTE, SBYTE);
		castExplicit(builtin, CHAR_TO_SHORT, SHORT);
		castExplicit(builtin, CHAR_TO_USHORT, USHORT);
		castImplicit(builtin, CHAR_TO_INT, INT);
		castImplicit(builtin, CHAR_TO_UINT, UINT);
		castImplicit(builtin, CHAR_TO_LONG, LONG);
		castImplicit(builtin, CHAR_TO_ULONG, ULONG);
		castImplicit(builtin, CHAR_TO_STRING, STRING);
		
		getter(builtin, CHAR_GET_MIN_VALUE, "MIN_VALUE", CHAR);
		getter(builtin, CHAR_GET_MAX_VALUE, "MAX_VALUE", CHAR);
		
		method(builtin, CHAR_REMOVE_DIACRITICS, "removeDiacritics", CHAR);
		method(builtin, CHAR_TO_LOWER_CASE, "toLowerCase", CHAR);
		method(builtin, CHAR_TO_UPPER_CASE, "toUpperCase", CHAR);
		
		processType(builtin, CHAR);
	}

	private void visitString() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, null, "string", Modifiers.EXPORT, null);
		
		constructor(builtin, STRING_CONSTRUCTOR_CHARACTERS, registry.getArray(CHAR, 1));
		
		add(builtin, STRING_ADD_STRING, STRING, STRING);
		indexGet(builtin, STRING_INDEXGET, INT, CHAR);
		indexGet(builtin, STRING_RANGEGET, registry.getRange(INT, INT), STRING);
		compare(builtin, STRING_COMPARE, STRING);
		
		getter(builtin, STRING_LENGTH, "length", INT);
		getter(builtin, STRING_CHARACTERS, "characters", registry.getArray(CHAR, 1));
		getter(builtin, STRING_ISEMPTY, "isEmpty", BOOL);

		method(builtin, STRING_REMOVE_DIACRITICS, "removeDiacritics", STRING);
		method(builtin, STRING_TRIM, "trim", STRING, STRING);
		method(builtin, STRING_TO_LOWER_CASE, "toLowerCase", STRING);
		method(builtin, STRING_TO_UPPER_CASE, "toUpperCase", STRING);
		
		new StringCharIterator().registerTo(members, TypeMemberPriority.SPECIFIED, null);
		
		processType(builtin, STRING);
	}
	
	private void castedTargetCall(OperatorMember member, BuiltinID casterBuiltin) {
		CasterMemberRef caster = castImplicitRef(member.definition, casterBuiltin, member.header.parameters[0].type);
		TranslatedOperatorMemberRef method = new TranslatedOperatorMemberRef(member, GenericMapper.EMPTY, call -> member.ref(null).call(call.position, caster.cast(call.position, call.target, true), call.arguments, call.scope));
		members.getOrCreateGroup(member.operator).addMethod(method, TypeMemberPriority.SPECIFIED);
	}
	
	private void register(IDefinitionMember member) {
		member.registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void operator(
			HighLevelDefinition definition,
			OperatorType operator,
			FunctionHeader header,
			BuiltinID builtin) {
		members.addOperator(operator, new OperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				operator,
				header,
				builtin).ref(null));
	}
	
	private void not(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		operator(cls, OperatorType.NOT, new FunctionHeader(result), id);
	}
	
	private void invert(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		operator(cls, OperatorType.INVERT, new FunctionHeader(result), id);
	}
	
	private void neg(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		operator(cls, OperatorType.NEG, new FunctionHeader(result), id);
	}
	
	private void inc(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		operator(cls, OperatorType.INCREMENT, new FunctionHeader(result), id);
	}
	
	private void dec(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		operator(cls, OperatorType.DECREMENT, new FunctionHeader(result), id);
	}
	
	private OperatorMember addOp(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.ADD,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void add(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result) {
		addOp(definition, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void add(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(addOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember subOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SUB,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void sub(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		subOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void sub(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(subOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember mulOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.MUL,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void mul(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		mulOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void mul(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(mulOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember divOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.DIV,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void div(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		divOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void div(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(divOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember modOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.MOD,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void mod(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		modOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void mod(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(modOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember shlOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SHL,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void shl(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		shlOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void shl(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(shlOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember shrOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC, 
				OperatorType.SHR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void shr(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		shrOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void shr(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(shrOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember ushrOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.USHR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void ushr(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		ushrOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void ushr(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(ushrOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember orOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.OR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void or(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		orOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void or(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		orOp(definition, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private OperatorMember andOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.AND,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void and(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		andOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void and(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(andOp(definition, id, operand, result), caster);
	}
	
	private OperatorMember xorOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.XOR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}
	
	private void xor(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		xorOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void xor(HighLevelDefinition definition, BuiltinID id, ITypeID operand, ITypeID result, BuiltinID caster) {
		castedTargetCall(xorOp(definition, id, operand, result), caster);
	}
	
	private void indexGet(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID result) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.INDEXGET,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void indexSet(HighLevelDefinition cls, BuiltinID id, ITypeID operand, ITypeID value) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.INDEXSET,
				new FunctionHeader(VOID, operand, value),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private OperatorMember compareOp(HighLevelDefinition cls, BuiltinID id, ITypeID operand) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.COMPARE,
				new FunctionHeader(INT, new FunctionParameter(operand)),
				id);
	}
	
	private void compare(HighLevelDefinition cls, BuiltinID id, ITypeID operand) {
		compareOp(cls, id, operand).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void compare(HighLevelDefinition definition, BuiltinID id, ITypeID operand, BuiltinID caster) {
		castedTargetCall(compareOp(definition, id, operand), caster);
	}
	
	private void getter(HighLevelDefinition cls, BuiltinID id, String name, ITypeID type) {
		new GetterMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				name,
				type,
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void constant(HighLevelDefinition cls, BuiltinID id, String name, Expression value) {
		ConstMember result = new ConstMember(
				BUILTIN,
				cls,
				Modifiers.STATIC | Modifiers.PUBLIC,
				name,
				value.type,
				id);
		result.value = value;
		result.registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void constructor(
			HighLevelDefinition definition,
			BuiltinID id,
			FunctionHeader header) {
		new ConstructorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				header,
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void constructor(
			HighLevelDefinition definition,
			BuiltinID id,
			ITypeID... arguments) {
		new ConstructorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				new FunctionHeader(VOID, arguments),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void method(
			HighLevelDefinition definition,
			String name,
			FunctionHeader header,
			BuiltinID builtin) {
		register(new MethodMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC | Modifiers.EXTERN,
				name,
				header,
				builtin));
	}
	
	private void method(
			ClassDefinition cls,
			BuiltinID id,
			String name,
			ITypeID result,
			ITypeID... arguments) {
		register(new MethodMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC | Modifiers.EXTERN,
				name,
				new FunctionHeader(result, arguments),
				id));
	}
	
	private void staticMethod(
			ClassDefinition cls,
			BuiltinID id,
			String name,
			ITypeID result,
			ITypeID... arguments) {
		register(new MethodMember(
				BUILTIN,
				cls,
				Modifiers.STATIC | Modifiers.PUBLIC | Modifiers.EXTERN,
				name,
				new FunctionHeader(result, arguments),
				id));
	}
	
	private void castExplicit(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		new CasterMember(
				CodePosition.BUILTIN,
				cls,
				Modifiers.PUBLIC,
				result,
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void castImplicit(HighLevelDefinition cls, BuiltinID id, ITypeID result) {
		new CasterMember(
				CodePosition.BUILTIN,
				cls,
				Modifiers.PUBLIC | Modifiers.IMPLICIT,
				result,
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private CasterMemberRef castImplicitRef(HighLevelDefinition definition, BuiltinID id, ITypeID result) {
		return new CasterMemberRef(new CasterMember(
				CodePosition.BUILTIN,
				definition,
				Modifiers.PUBLIC | Modifiers.IMPLICIT,
				result,
				id), result);
	}
	
	private void equals(HighLevelDefinition cls, BuiltinID id, ITypeID type) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.EQUALS,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void same(HighLevelDefinition cls, BuiltinID id, ITypeID type) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SAME,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void notequals(HighLevelDefinition cls, BuiltinID id, ITypeID type) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.NOTEQUALS,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
	
	private void notsame(HighLevelDefinition cls, BuiltinID id, ITypeID type) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.NOTSAME,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
}
