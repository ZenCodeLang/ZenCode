package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.member.ref.CasterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.TranslatedOperatorMemberRef;
import org.openzen.zenscript.codemodel.type.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.openzen.zencode.shared.CodePosition.BUILTIN;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;
import static org.openzen.zenscript.codemodel.type.member.BuiltinID.*;

public class TypeMemberBuilder implements TypeVisitorWithContext<Void, Void, RuntimeException> {
	private final GlobalTypeRegistry registry;
	private final TypeMembers members;
	private final LocalMemberCache cache;
	private final TypeID type;

	public TypeMemberBuilder(GlobalTypeRegistry registry, TypeMembers members, LocalMemberCache cache) {
		this.registry = registry;
		this.members = members;
		this.cache = cache;

		type = members.type;
	}

	private void processType(HighLevelDefinition definition) {
		for (ExpansionDefinition expansion : cache.getExpansions()) {
			if (expansion.target == null)
				throw new RuntimeException(expansion.position.toString() + ": Missing expansion target");

			Map<TypeParameter, TypeID> mapping = matchType(type, expansion.target);
			if (mapping == null)
				continue;

			GenericMapper mapper = new GenericMapper(definition.position, registry, mapping);
			for (IDefinitionMember member : expansion.members)
				member.registerTo(members, TypeMemberPriority.SPECIFIED, mapper);
		}
	}

	private Map<TypeParameter, TypeID> matchType(TypeID type, TypeID pattern) {
		return type.inferTypeParameters(cache, pattern);
	}

	@Override
	public Void visitBasic(Void context, BasicTypeID basic) {
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
			case USIZE:
				visitUSize();
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
		}

		return null;
	}

	private void visitString() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "string", Modifiers.PUBLIC, null);

		constructor(builtin, STRING_CONSTRUCTOR_CHARACTERS, registry.getOptional(registry.getArray(CHAR, 1)));

		add(builtin, STRING_ADD_STRING, STRING, STRING);
		indexGet(builtin, STRING_INDEXGET, USIZE, CHAR);
		indexGet(builtin, STRING_RANGEGET, RangeTypeID.USIZE, STRING);
		compare(builtin, STRING_COMPARE, STRING);

		getter(builtin, STRING_LENGTH, "length", USIZE);
		getter(builtin, STRING_CHARACTERS, "characters", registry.getArray(CHAR, 1));
		// TODO remove in favour of empty
		getter(builtin, STRING_ISEMPTY, "isEmpty", BOOL);
		getter(builtin, STRING_ISEMPTY, "empty", BOOL);


		method(builtin, STRING_REMOVE_DIACRITICS, "removeDiacritics", STRING);
		method(builtin, STRING_TRIM, "trim", STRING, STRING);
		method(builtin, STRING_TO_LOWER_CASE, "toLowerCase", STRING);
		method(builtin, STRING_TO_UPPER_CASE, "toUpperCase", STRING);

		iterator(builtin, ITERATOR_STRING_CHARS, CHAR);

		processType(builtin);
	}

	@Override
	public Void visitArray(Void context, ArrayTypeID array) {
		HighLevelDefinition definition = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "", Modifiers.PUBLIC);
		TypeID baseType = array.elementType;
		int dimension = array.dimension;

		FunctionParameter[] indexGetParameters = new FunctionParameter[dimension];
		for (int i = 0; i < indexGetParameters.length; i++)
			indexGetParameters[i] = new FunctionParameter(USIZE);

		operator(
				definition,
				OperatorType.INDEXGET,
				new FunctionHeader(baseType, indexGetParameters),
				ARRAY_INDEXGET);

		if (dimension == 1) {
			FunctionHeader sliceHeader = new FunctionHeader(type, new FunctionParameter(RangeTypeID.USIZE, "range"));
			operator(
					definition,
					OperatorType.INDEXGET,
					sliceHeader,
					ARRAY_INDEXGETRANGE);

			if (baseType == BYTE)
				castImplicit(definition, BYTE_ARRAY_AS_SBYTE_ARRAY, registry.getArray(SBYTE, 1));
			if (baseType == SBYTE)
				castImplicit(definition, SBYTE_ARRAY_AS_BYTE_ARRAY, registry.getArray(BYTE, 1));
			if (baseType == SHORT)
				castImplicit(definition, SHORT_ARRAY_AS_USHORT_ARRAY, registry.getArray(USHORT, 1));
			if (baseType == USHORT)
				castImplicit(definition, USHORT_ARRAY_AS_SHORT_ARRAY, registry.getArray(SHORT, 1));
			if (baseType == INT)
				castImplicit(definition, INT_ARRAY_AS_UINT_ARRAY, registry.getArray(UINT, 1));
			if (baseType == UINT)
				castImplicit(definition, UINT_ARRAY_AS_INT_ARRAY, registry.getArray(INT, 1));
			if (baseType == LONG)
				castImplicit(definition, LONG_ARRAY_AS_ULONG_ARRAY, registry.getArray(ULONG, 1));
			if (baseType == ULONG)
				castImplicit(definition, ULONG_ARRAY_AS_LONG_ARRAY, registry.getArray(LONG, 1));
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
					Modifiers.PUBLIC,
					new FunctionHeader(VOID, indexGetParameters),
					ARRAY_CONSTRUCTOR_SIZED).ref(type));
		}

		FunctionParameter[] initialValueConstructorParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			initialValueConstructorParameters[i] = new FunctionParameter(USIZE);
		initialValueConstructorParameters[dimension] = new FunctionParameter(baseType);
		FunctionHeader initialValueConstructorHeader = new FunctionHeader(VOID, initialValueConstructorParameters);
		new ConstructorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				initialValueConstructorHeader,
				ARRAY_CONSTRUCTOR_INITIAL_VALUE)
				.registerTo(members, TypeMemberPriority.SPECIFIED, null);

		FunctionParameter[] lambdaConstructorParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			lambdaConstructorParameters[i] = new FunctionParameter(USIZE);

		FunctionHeader lambdaConstructorFunction = new FunctionHeader(baseType, indexGetParameters);
		lambdaConstructorParameters[dimension] = new FunctionParameter(cache.getRegistry().getFunction(lambdaConstructorFunction));
		FunctionHeader lambdaConstructorHeader = new FunctionHeader(VOID, lambdaConstructorParameters);
		members.addConstructor(new ConstructorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				lambdaConstructorHeader,
				ARRAY_CONSTRUCTOR_LAMBDA).ref(type));

		{
			TypeParameter mappedConstructorParameter = new TypeParameter(BUILTIN, "T");
			FunctionHeader mappedConstructorHeaderWithoutIndex = new FunctionHeader(baseType, registry.getGeneric(mappedConstructorParameter));
			FunctionHeader mappedConstructorFunctionWithoutIndex = new FunctionHeader(
					new TypeParameter[]{mappedConstructorParameter},
					VOID,
					null,
					new FunctionParameter(registry.getArray(registry.getGeneric(mappedConstructorParameter), dimension), "original"),
					new FunctionParameter(registry.getFunction(mappedConstructorHeaderWithoutIndex), "projection"));
			members.addConstructor(new ConstructorMember(
					BUILTIN,
					definition,
					Modifiers.PUBLIC,
					mappedConstructorFunctionWithoutIndex,
					ARRAY_CONSTRUCTOR_PROJECTED).ref(type));
		}

		{
			TypeParameter mappedConstructorParameter = new TypeParameter(BUILTIN, "T");
			FunctionParameter[] projectionParameters = new FunctionParameter[dimension + 1];
			for (int i = 0; i < dimension; i++)
				projectionParameters[i] = new FunctionParameter(USIZE);
			projectionParameters[dimension] = new FunctionParameter(registry.getGeneric(mappedConstructorParameter));

			FunctionHeader mappedConstructorHeaderWithIndex = new FunctionHeader(baseType, projectionParameters);
			FunctionHeader mappedConstructorFunctionWithIndex = new FunctionHeader(
					new TypeParameter[]{mappedConstructorParameter},
					VOID,
					null,
					new FunctionParameter(registry.getArray(registry.getGeneric(mappedConstructorParameter), dimension), "original"),
					new FunctionParameter(registry.getFunction(mappedConstructorHeaderWithIndex), "projection"));
			constructor(definition, ARRAY_CONSTRUCTOR_PROJECTED_INDEXED, mappedConstructorFunctionWithIndex);
		}

		FunctionParameter[] indexSetParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			indexSetParameters[i] = new FunctionParameter(USIZE);
		indexSetParameters[dimension] = new FunctionParameter(baseType);

		FunctionHeader indexSetHeader = new FunctionHeader(VOID, indexSetParameters);
		operator(definition, OperatorType.INDEXSET, indexSetHeader, ARRAY_INDEXSET);

		if (dimension == 1) {
			getter(definition, ARRAY_LENGTH, "length", USIZE);
		}

		getter(definition, ARRAY_ISEMPTY, "isEmpty", BOOL);
		getter(definition, ARRAY_HASHCODE, "objectHashCode", INT);
		iterator(definition, ITERATOR_ARRAY_VALUES, baseType);
		iterator(definition, ITERATOR_ARRAY_KEY_VALUES, USIZE, baseType);

		equals(definition, ARRAY_EQUALS, type);
		notequals(definition, ARRAY_NOTEQUALS, type);
		same(definition, ARRAY_SAME, type);
		notsame(definition, ARRAY_NOTSAME, type);

		processType(definition);
		return null;
	}

	@Override
	public Void visitAssoc(Void context, AssocTypeID assoc) {
		TypeID keyType = assoc.keyType;
		TypeID valueType = assoc.valueType;

		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "", Modifiers.PUBLIC);

		constructor(builtin, ASSOC_CONSTRUCTOR);

		indexGet(builtin, ASSOC_INDEXGET, keyType, valueType);
		indexSet(builtin, ASSOC_INDEXSET, keyType, valueType);

		method(builtin, ASSOC_GETORDEFAULT, "getOrDefault", valueType, keyType, valueType);

		operator(
				builtin,
				OperatorType.CONTAINS,
				new FunctionHeader(BOOL, new FunctionParameter(keyType, "key")),
				ASSOC_CONTAINS);

		getter(builtin, BuiltinID.ASSOC_SIZE, "size", USIZE);
		getter(builtin, BuiltinID.ASSOC_ISEMPTY, "isEmpty", BOOL);
		getter(builtin, BuiltinID.ASSOC_KEYS, "keys", cache.getRegistry().getArray(keyType, 1));
		getter(builtin, BuiltinID.ASSOC_VALUES, "values", cache.getRegistry().getArray(valueType, 1));
		getter(builtin, BuiltinID.ASSOC_HASHCODE, "objectHashCode", INT);

		iterator(builtin, ITERATOR_ASSOC_KEYS, keyType);
		iterator(builtin, ITERATOR_ASSOC_KEY_VALUES, keyType, valueType);

		equals(builtin, BuiltinID.ASSOC_EQUALS, type);
		notequals(builtin, BuiltinID.ASSOC_NOTEQUALS, type);
		same(builtin, BuiltinID.ASSOC_SAME, type);
		notsame(builtin, BuiltinID.ASSOC_NOTSAME, type);

		processType(builtin);
		return null;
	}

	@Override
	public Void visitGenericMap(Void context, GenericMapTypeID map) {
		TypeParameter functionParameter = new TypeParameter(BUILTIN, "T");
		Map<TypeParameter, TypeID> parameterFilled = Collections.singletonMap(map.key, registry.getGeneric(functionParameter));
		TypeID valueType = map.value.instance(new GenericMapper(CodePosition.BUILTIN, registry, parameterFilled));

		FunctionHeader getOptionalHeader = new FunctionHeader(
				new TypeParameter[]{functionParameter},
				registry.getOptional(valueType),
				null,
				FunctionParameter.NONE);
		FunctionHeader putHeader = new FunctionHeader(new TypeParameter[]{functionParameter}, VOID, null, new FunctionParameter(valueType));
		FunctionHeader containsHeader = new FunctionHeader(new TypeParameter[]{functionParameter}, BOOL, null, FunctionParameter.NONE);

		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "", Modifiers.PUBLIC);
		constructor(builtin, GENERICMAP_CONSTRUCTOR);

		method(builtin, "getOptional", getOptionalHeader, GENERICMAP_GETOPTIONAL);
		method(builtin, "put", putHeader, GENERICMAP_PUT);
		method(builtin, "contains", containsHeader, GENERICMAP_CONTAINS);
		method(builtin, "addAll", new FunctionHeader(VOID, type), GENERICMAP_ADDALL);

		getter(builtin, GENERICMAP_SIZE, "size", USIZE);
		getter(builtin, GENERICMAP_ISEMPTY, "isEmpty", BOOL);
		getter(builtin, GENERICMAP_HASHCODE, "objectHashCode", INT);

		equals(builtin, GENERICMAP_EQUALS, type);
		notequals(builtin, GENERICMAP_NOTEQUALS, type);
		same(builtin, GENERICMAP_SAME, type);
		notsame(builtin, GENERICMAP_NOTSAME, type);

		processType(builtin);
		return null;
	}

	@Override
	public Void visitInvalid(Void context, InvalidTypeID invalid) {
		return null;
	}

	@Override
	public Void visitIterator(Void context, IteratorTypeID iterator) {
		return null;
	}

	@Override
	public Void visitFunction(Void context, FunctionTypeID function) {
		FunctionDefinition builtin = new FunctionDefinition(BUILTIN, Module.BUILTIN, null, "", Modifiers.PUBLIC, function.header, registry);
		new CallerMember(BUILTIN, builtin, Modifiers.PUBLIC, function.header, FUNCTION_CALL).registerTo(members, TypeMemberPriority.SPECIFIED, null);

		same(builtin, FUNCTION_SAME, type);
		notsame(builtin, FUNCTION_NOTSAME, type);

		processType(builtin);
		return null;
	}

	@Override
	public Void visitDefinition(Void context, DefinitionTypeID definitionType) {
		HighLevelDefinition definition = definitionType.definition;
		GenericMapper mapper = null;
		if (definitionType.hasTypeParameters() || (definitionType.outer != null && definitionType.outer.hasTypeParameters())) {
			Map<TypeParameter, TypeID> mapping = definitionType.getTypeParameterMapping();
			mapper = new GenericMapper(CodePosition.BUILTIN, registry, mapping);
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

		TypeMemberGroup constructors = members.getOrCreateGroup(OperatorType.CONSTRUCTOR);
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
						parameters[i] = new FunctionParameter(field.getType(), field.name, field.initializer, false);
					}

					constructors.addMethod(new ConstructorMember(
							BUILTIN,
							definition,
							Modifiers.PUBLIC,
							new FunctionHeader(VOID, parameters),
							STRUCT_VALUE_CONSTRUCTOR).ref(type), TypeMemberPriority.SPECIFIED);
				}
			} else if (definition instanceof EnumDefinition) {
				// add default constructor
				constructors.addMethod(new ConstructorMember(
						BUILTIN,
						definition,
						Modifiers.PRIVATE,
						new FunctionHeader(VOID),
						ENUM_EMPTY_CONSTRUCTOR).ref(type), TypeMemberPriority.SPECIFIED);
			}
		}

		if (definition instanceof EnumDefinition) {
			getter(definition, ENUM_NAME, "name", STRING);
			getter(definition, ENUM_ORDINAL, "ordinal", USIZE);

			List<EnumConstantMember> enumConstants = ((EnumDefinition) definition).enumConstants;
			Expression[] constValues = new Expression[enumConstants.size()];
			for (int i = 0; i < constValues.length; i++)
				constValues[i] = new EnumConstantExpression(BUILTIN, definitionType, enumConstants.get(i));

			constant(definition, ENUM_VALUES, "values", new ArrayExpression(BUILTIN, constValues, registry.getArray(definitionType, 1)));
			compare(definition, ENUM_COMPARE, type);

			if (!members.canCast(STRING)) {
				castImplicit(definition, ENUM_TO_STRING, STRING);
			}
		}

		if (definition instanceof InterfaceDefinition) {
			InterfaceDefinition interfaceDefinition = (InterfaceDefinition) definition;
			for (TypeID baseType : interfaceDefinition.baseInterfaces)
				cache.get(baseType.instance(mapper)).copyMembersTo(members, TypeMemberPriority.INHERITED);
		}

		if (definition.getSuperType() != null) {
			cache.get(definition.getSuperType()).copyMembersTo(members, TypeMemberPriority.INHERITED);
		} else {
			getter(definition, OBJECT_HASHCODE, "objectHashCode", INT);
		}

		same(definition, OBJECT_SAME, type);
		notsame(definition, OBJECT_NOTSAME, type);

		processType(definition);
		return null;
	}

	@Override
	public Void visitGeneric(Void context, GenericTypeID generic) {
		TypeParameter parameter = generic.parameter;

		for (TypeParameterBound bound : parameter.bounds) {
			bound.registerMembers(cache, members);
		}

		return null;
	}

	@Override
	public Void visitRange(Void context, RangeTypeID range) {
		TypeID baseType = range.baseType;

		ClassDefinition definition = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "", Modifiers.PUBLIC);
		getter(definition, RANGE_FROM, "from", baseType);
		getter(definition, RANGE_TO, "to", baseType);
		if (baseType == BYTE
				|| baseType == SBYTE
				|| baseType == SHORT
				|| baseType == USHORT
				|| baseType == INT
				|| baseType == UINT
				|| baseType == LONG
				|| baseType == ULONG
				|| baseType == USIZE) {

			iterator(definition, ITERATOR_INT_RANGE, baseType);
		}

		processType(definition);
		return null;
	}

	@Override
	public Void visitOptional(Void context, OptionalTypeID modified) {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "modified", Modifiers.PUBLIC, null);
		modified.baseType.accept(context, this);

		if (modified.isOptional()) {
			operator(builtin, OperatorType.EQUALS, new FunctionHeader(BOOL, NULL), BuiltinID.OPTIONAL_IS_NULL);
			operator(builtin, OperatorType.NOTEQUALS, new FunctionHeader(BOOL, NULL), BuiltinID.OPTIONAL_IS_NOT_NULL);
			operator(builtin, OperatorType.SAME, new FunctionHeader(BOOL, NULL), BuiltinID.OPTIONAL_IS_NULL);
			operator(builtin, OperatorType.NOTSAME, new FunctionHeader(BOOL, NULL), BuiltinID.OPTIONAL_IS_NOT_NULL);
		}

		processType(builtin);
		return null;
	}

	private void visitBool() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "bool", Modifiers.PUBLIC, null);
		not(builtin, BOOL_NOT, BOOL);
		and(builtin, BOOL_AND, BOOL, BOOL);
		or(builtin, BOOL_OR, BOOL, BOOL);
		xor(builtin, BOOL_XOR, BOOL, BOOL);
		equals(builtin, BOOL_EQUALS, BOOL);
		notequals(builtin, BOOL_NOTEQUALS, BOOL);

		castImplicit(builtin, BOOL_TO_STRING, STRING);
		staticMethod(builtin, BOOL_PARSE, "parse", BOOL, STRING);

		processType(builtin);
	}

	private void visitByte() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "byte", Modifiers.PUBLIC, null);

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
		shl(builtin, BYTE_SHL, USIZE, BYTE);
		shr(builtin, BYTE_SHR, USIZE, BYTE);
		compare(builtin, BYTE_COMPARE, BYTE);

		castImplicit(builtin, BYTE_TO_SBYTE, SBYTE);
		castImplicit(builtin, BYTE_TO_SHORT, SHORT);
		castImplicit(builtin, BYTE_TO_USHORT, USHORT);
		castImplicit(builtin, BYTE_TO_INT, INT);
		castImplicit(builtin, BYTE_TO_UINT, UINT);
		castImplicit(builtin, BYTE_TO_LONG, LONG);
		castImplicit(builtin, BYTE_TO_ULONG, ULONG);
		castImplicit(builtin, BYTE_TO_USIZE, USIZE);
		castImplicit(builtin, BYTE_TO_FLOAT, FLOAT);
		castImplicit(builtin, BYTE_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, BYTE_TO_CHAR, CHAR);
		castImplicit(builtin, BYTE_TO_STRING, STRING);

		staticMethod(builtin, BYTE_PARSE, "parse", BYTE, STRING);
		staticMethod(builtin, BYTE_PARSE_WITH_BASE, "parse", BYTE, STRING, INT);

		constant(builtin, BYTE_GET_MIN_VALUE, "MIN_VALUE", new ConstantByteExpression(BUILTIN, 0));
		constant(builtin, BYTE_GET_MAX_VALUE, "MAX_VALUE", new ConstantByteExpression(BUILTIN, 255));

		processType(builtin);
	}

	private void visitSByte() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "sbyte", Modifiers.PUBLIC, null);

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
		shl(builtin, SBYTE_SHL, USIZE, SBYTE);
		shr(builtin, SBYTE_SHR, USIZE, SBYTE);
		ushr(builtin, SBYTE_USHR, USIZE, SBYTE);
		compare(builtin, SBYTE_COMPARE, SBYTE);

		castImplicit(builtin, SBYTE_TO_BYTE, BYTE);
		castImplicit(builtin, SBYTE_TO_SHORT, SHORT);
		castImplicit(builtin, SBYTE_TO_USHORT, USHORT);
		castImplicit(builtin, SBYTE_TO_INT, INT);
		castImplicit(builtin, SBYTE_TO_UINT, UINT);
		castImplicit(builtin, SBYTE_TO_LONG, LONG);
		castImplicit(builtin, SBYTE_TO_ULONG, ULONG);
		castImplicit(builtin, SBYTE_TO_USIZE, USIZE);
		castImplicit(builtin, SBYTE_TO_FLOAT, FLOAT);
		castImplicit(builtin, SBYTE_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, SBYTE_TO_CHAR, CHAR);
		castImplicit(builtin, SBYTE_TO_STRING, STRING);

		staticMethod(builtin, SBYTE_PARSE, "parse", SBYTE, STRING);
		staticMethod(builtin, SBYTE_PARSE_WITH_BASE, "parse", SBYTE, STRING, INT);

		constant(builtin, SBYTE_GET_MIN_VALUE, "MIN_VALUE", new ConstantSByteExpression(BUILTIN, Byte.MIN_VALUE));
		constant(builtin, SBYTE_GET_MAX_VALUE, "MAX_VALUE", new ConstantSByteExpression(BUILTIN, Byte.MAX_VALUE));

		processType(builtin);
	}

	private void visitShort() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "short", Modifiers.PUBLIC, null);

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
		shl(builtin, SHORT_SHL, USIZE, SHORT);
		shr(builtin, SHORT_SHR, USIZE, SHORT);
		ushr(builtin, SHORT_USHR, USIZE, SHORT);
		compare(builtin, SHORT_COMPARE, SHORT);

		castExplicit(builtin, SHORT_TO_BYTE, BYTE);
		castExplicit(builtin, SHORT_TO_SBYTE, SBYTE);
		castImplicit(builtin, SHORT_TO_USHORT, USHORT);
		castImplicit(builtin, SHORT_TO_INT, INT);
		castImplicit(builtin, SHORT_TO_UINT, UINT);
		castImplicit(builtin, SHORT_TO_LONG, LONG);
		castImplicit(builtin, SHORT_TO_ULONG, ULONG);
		castImplicit(builtin, SHORT_TO_USIZE, USIZE);
		castImplicit(builtin, SHORT_TO_FLOAT, FLOAT);
		castImplicit(builtin, SHORT_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, SHORT_TO_CHAR, CHAR);
		castImplicit(builtin, SHORT_TO_STRING, STRING);

		staticMethod(builtin, SHORT_PARSE, "parse", SHORT, STRING);
		staticMethod(builtin, SHORT_PARSE_WITH_BASE, "parse", SHORT, STRING, INT);

		constant(builtin, SHORT_GET_MIN_VALUE, "MIN_VALUE", new ConstantShortExpression(BUILTIN, Short.MIN_VALUE));
		constant(builtin, SHORT_GET_MAX_VALUE, "MAX_VALUE", new ConstantShortExpression(BUILTIN, Short.MAX_VALUE));

		processType(builtin);
	}

	private void visitUShort() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "ushort", Modifiers.PUBLIC, null);

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
		shl(builtin, USHORT_SHL, USIZE, USHORT);
		shr(builtin, USHORT_SHR, USIZE, USHORT);
		compare(builtin, USHORT_COMPARE, USHORT);

		castExplicit(builtin, USHORT_TO_BYTE, BYTE);
		castExplicit(builtin, USHORT_TO_SBYTE, SBYTE);
		castImplicit(builtin, USHORT_TO_SHORT, SHORT);
		castImplicit(builtin, USHORT_TO_INT, INT);
		castImplicit(builtin, USHORT_TO_UINT, UINT);
		castImplicit(builtin, USHORT_TO_LONG, LONG);
		castImplicit(builtin, USHORT_TO_ULONG, ULONG);
		castImplicit(builtin, USHORT_TO_USIZE, USIZE);
		castImplicit(builtin, USHORT_TO_FLOAT, FLOAT);
		castImplicit(builtin, USHORT_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, USHORT_TO_CHAR, CHAR);
		castImplicit(builtin, USHORT_TO_STRING, STRING);

		staticMethod(builtin, USHORT_PARSE, "parse", USHORT, STRING);
		staticMethod(builtin, USHORT_PARSE_WITH_BASE, "parse", USHORT, STRING, INT);

		constant(builtin, USHORT_GET_MIN_VALUE, "MIN_VALUE", new ConstantUShortExpression(BUILTIN, 0));
		constant(builtin, USHORT_GET_MAX_VALUE, "MAX_VALUE", new ConstantUShortExpression(BUILTIN, 65535));

		processType(builtin);
	}

	private void visitInt() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "int", Modifiers.PUBLIC, null);

		invert(builtin, INT_NOT, INT);
		neg(builtin, INT_NEG, INT);
		inc(builtin, INT_INC, INT);
		dec(builtin, INT_DEC, INT);

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

		shl(builtin, INT_SHL, USIZE, INT);
		shr(builtin, INT_SHR, USIZE, INT);
		ushr(builtin, INT_USHR, USIZE, INT);

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
		castImplicit(builtin, INT_TO_USIZE, USIZE);
		castImplicit(builtin, INT_TO_FLOAT, FLOAT);
		castImplicit(builtin, INT_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, INT_TO_CHAR, CHAR);
		castImplicit(builtin, INT_TO_STRING, STRING);

		staticMethod(builtin, INT_PARSE, "parse", INT, STRING);
		staticMethod(builtin, INT_PARSE_WITH_BASE, "parse", INT, STRING, INT);

		method(builtin, INT_COUNT_LOW_ZEROES, "countLowZeroes", USIZE);
		method(builtin, INT_COUNT_HIGH_ZEROES, "countHighZeroes", USIZE);
		method(builtin, INT_COUNT_LOW_ONES, "countLowOnes", USIZE);
		method(builtin, INT_COUNT_HIGH_ONES, "countHighOnes", USIZE);

		TypeID optionalUSize = registry.getOptional(USIZE);
		getter(builtin, INT_HIGHEST_ONE_BIT, "highestOneBit", optionalUSize);
		getter(builtin, INT_LOWEST_ONE_BIT, "lowestOneBit", optionalUSize);
		getter(builtin, INT_HIGHEST_ZERO_BIT, "highestZeroBit", optionalUSize);
		getter(builtin, INT_LOWEST_ZERO_BIT, "lowestZeroBit", optionalUSize);
		getter(builtin, INT_BIT_COUNT, "bitCount", USIZE);

		processType(builtin);
	}

	private void visitUInt() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "uint", Modifiers.PUBLIC, null);

		invert(builtin, UINT_NOT, INT);
		inc(builtin, UINT_INC, INT);
		dec(builtin, UINT_DEC, INT);

		add(builtin, UINT_ADD_UINT, UINT, UINT);
		add(builtin, ULONG_ADD_ULONG, USIZE, ULONG, UINT_TO_ULONG);
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

		shl(builtin, UINT_SHL, USIZE, UINT);
		shr(builtin, UINT_SHR, USIZE, UINT);

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
		castImplicit(builtin, UINT_TO_USIZE, USIZE);
		castImplicit(builtin, UINT_TO_FLOAT, FLOAT);
		castImplicit(builtin, UINT_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, UINT_TO_CHAR, CHAR);
		castImplicit(builtin, UINT_TO_STRING, STRING);

		staticMethod(builtin, UINT_PARSE, "parse", UINT, STRING);
		staticMethod(builtin, UINT_PARSE_WITH_BASE, "parse", UINT, STRING, INT);

		method(builtin, UINT_COUNT_LOW_ZEROES, "countLowZeroes", USIZE);
		method(builtin, UINT_COUNT_HIGH_ZEROES, "countHighZeroes", USIZE);
		method(builtin, UINT_COUNT_LOW_ONES, "countLowOnes", USIZE);
		method(builtin, UINT_COUNT_HIGH_ONES, "countHighOnes", USIZE);

		TypeID optionalUSize = registry.getOptional(USIZE);
		getter(builtin, UINT_HIGHEST_ONE_BIT, "highestOneBit", optionalUSize);
		getter(builtin, UINT_LOWEST_ONE_BIT, "lowestOneBit", optionalUSize);
		getter(builtin, UINT_HIGHEST_ZERO_BIT, "highestZeroBit", optionalUSize);
		getter(builtin, UINT_LOWEST_ZERO_BIT, "lowestZeroBit", optionalUSize);
		getter(builtin, UINT_BIT_COUNT, "bitCount", USIZE);

		processType(builtin);
	}

	private void visitLong() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "long", Modifiers.PUBLIC, null);

		invert(builtin, LONG_NOT, LONG);
		neg(builtin, LONG_NEG, LONG);
		inc(builtin, LONG_INC, LONG);
		dec(builtin, LONG_DEC, LONG);

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

		shl(builtin, LONG_SHL, USIZE, LONG);
		shr(builtin, LONG_SHR, USIZE, LONG);
		ushr(builtin, LONG_USHR, USIZE, LONG);

		compare(builtin, LONG_COMPARE_INT, INT);
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
		castExplicit(builtin, LONG_TO_USIZE, USIZE);
		castImplicit(builtin, LONG_TO_FLOAT, FLOAT);
		castImplicit(builtin, LONG_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, LONG_TO_CHAR, CHAR);
		castImplicit(builtin, LONG_TO_STRING, STRING);

		staticMethod(builtin, LONG_PARSE, "parse", LONG, STRING);
		staticMethod(builtin, LONG_PARSE_WITH_BASE, "parse", LONG, STRING, INT);

		method(builtin, LONG_COUNT_LOW_ZEROES, "countLowZeroes", USIZE);
		method(builtin, LONG_COUNT_HIGH_ZEROES, "countHighZeroes", USIZE);
		method(builtin, LONG_COUNT_LOW_ONES, "countLowOnes", USIZE);
		method(builtin, LONG_COUNT_HIGH_ONES, "countHighOnes", USIZE);

		TypeID optionalUSize = registry.getOptional(USIZE);
		getter(builtin, LONG_HIGHEST_ONE_BIT, "highestOneBit", optionalUSize);
		getter(builtin, LONG_LOWEST_ONE_BIT, "lowestOneBit", optionalUSize);
		getter(builtin, LONG_HIGHEST_ZERO_BIT, "highestZeroBit", optionalUSize);
		getter(builtin, LONG_LOWEST_ZERO_BIT, "lowestZeroBit", optionalUSize);
		getter(builtin, LONG_BIT_COUNT, "bitCount", USIZE);

		processType(builtin);
	}

	private void visitULong() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "ulong", Modifiers.PUBLIC, null);

		invert(builtin, ULONG_NOT, ULONG);
		inc(builtin, ULONG_INC, ULONG);
		dec(builtin, ULONG_DEC, ULONG);

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

		shl(builtin, ULONG_SHL, USIZE, ULONG);
		shr(builtin, ULONG_SHR, USIZE, ULONG);

		compare(builtin, ULONG_COMPARE_UINT, UINT);
		compare(builtin, ULONG_COMPARE_USIZE, USIZE);
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
		castExplicit(builtin, ULONG_TO_USIZE, USIZE);
		castImplicit(builtin, ULONG_TO_FLOAT, FLOAT);
		castImplicit(builtin, ULONG_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, ULONG_TO_CHAR, CHAR);
		castImplicit(builtin, ULONG_TO_STRING, STRING);

		staticMethod(builtin, ULONG_PARSE, "parse", ULONG, STRING);
		staticMethod(builtin, ULONG_PARSE_WITH_BASE, "parse", ULONG, STRING, INT);

		method(builtin, ULONG_COUNT_LOW_ZEROES, "countLowZeroes", USIZE);
		method(builtin, ULONG_COUNT_HIGH_ZEROES, "countHighZeroes", USIZE);
		method(builtin, ULONG_COUNT_LOW_ONES, "countLowOnes", USIZE);
		method(builtin, ULONG_COUNT_HIGH_ONES, "countHighOnes", USIZE);

		TypeID optionalUSize = registry.getOptional(USIZE);
		getter(builtin, ULONG_HIGHEST_ONE_BIT, "highestOneBit", optionalUSize);
		getter(builtin, ULONG_LOWEST_ONE_BIT, "lowestOneBit", optionalUSize);
		getter(builtin, ULONG_HIGHEST_ZERO_BIT, "highestZeroBit", optionalUSize);
		getter(builtin, ULONG_LOWEST_ZERO_BIT, "lowestZeroBit", optionalUSize);
		getter(builtin, ULONG_BIT_COUNT, "bitCount", USIZE);

		processType(builtin);
	}

	private void visitUSize() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "usize", Modifiers.PUBLIC, null);

		invert(builtin, USIZE_NOT, USIZE);
		inc(builtin, USIZE_INC, USIZE);
		dec(builtin, USIZE_DEC, USIZE);

		add(builtin, USIZE_ADD_USIZE, USIZE, USIZE);
		add(builtin, ULONG_ADD_ULONG, ULONG, ULONG, USIZE_TO_ULONG);
		add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT, USIZE_TO_FLOAT);
		add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, USIZE_TO_DOUBLE);

		sub(builtin, USIZE_SUB_USIZE, USIZE, USIZE);
		sub(builtin, ULONG_SUB_ULONG, ULONG, ULONG, USIZE_TO_ULONG);
		sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT, USIZE_TO_FLOAT);
		sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, USIZE_TO_DOUBLE);

		mul(builtin, USIZE_MUL_USIZE, USIZE, USIZE);
		mul(builtin, ULONG_MUL_ULONG, ULONG, ULONG, USIZE_TO_ULONG);
		mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT, USIZE_TO_FLOAT);
		mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, USIZE_TO_DOUBLE);

		div(builtin, USIZE_DIV_USIZE, USIZE, USIZE);
		div(builtin, ULONG_DIV_ULONG, ULONG, ULONG, USIZE_TO_ULONG);
		div(builtin, FLOAT_DIV_FLOAT, FLOAT, FLOAT, USIZE_TO_FLOAT);
		div(builtin, DOUBLE_DIV_DOUBLE, DOUBLE, DOUBLE, USIZE_TO_DOUBLE);

		mod(builtin, USIZE_MOD_USIZE, USIZE, USIZE);

		or(builtin, USIZE_OR_USIZE, USIZE, USIZE);
		and(builtin, USIZE_AND_USIZE, USIZE, USIZE);
		xor(builtin, USIZE_XOR_USIZE, USIZE, USIZE);

		shl(builtin, USIZE_SHL, USIZE, USIZE);
		shr(builtin, USIZE_SHR, USIZE, USIZE);

		compare(builtin, USIZE_COMPARE_UINT, UINT);
		compare(builtin, USIZE_COMPARE, USIZE);
		compare(builtin, ULONG_COMPARE, ULONG, USIZE_TO_ULONG);
		compare(builtin, FLOAT_COMPARE, FLOAT, USIZE_TO_FLOAT);
		compare(builtin, DOUBLE_COMPARE, DOUBLE, USIZE_TO_DOUBLE);

		constant(builtin, USIZE_GET_MIN_VALUE, "MIN_VALUE", new ConstantUSizeExpression(BUILTIN, 0));
		constant(builtin, USIZE_GET_MAX_VALUE, "MAX_VALUE", new ConstantUSizeExpression(BUILTIN, -2L));
		constant(builtin, USIZE_BITS, "BITS", new ConstantUSizeExpression(BUILTIN, 32));

		castExplicit(builtin, USIZE_TO_BYTE, BYTE);
		castExplicit(builtin, USIZE_TO_SBYTE, SBYTE);
		castExplicit(builtin, USIZE_TO_SHORT, SHORT);
		castExplicit(builtin, USIZE_TO_USHORT, USHORT);
		castExplicit(builtin, USIZE_TO_INT, INT);
		castExplicit(builtin, USIZE_TO_UINT, UINT);
		castImplicit(builtin, USIZE_TO_LONG, LONG);
		castImplicit(builtin, USIZE_TO_ULONG, ULONG);
		castImplicit(builtin, USIZE_TO_FLOAT, FLOAT);
		castImplicit(builtin, USIZE_TO_DOUBLE, DOUBLE);
		castExplicit(builtin, USIZE_TO_CHAR, CHAR);
		castImplicit(builtin, USIZE_TO_STRING, STRING);

		staticMethod(builtin, USIZE_PARSE, "parse", USIZE, STRING);
		staticMethod(builtin, USIZE_PARSE_WITH_BASE, "parse", USIZE, STRING, INT);

		method(builtin, USIZE_COUNT_LOW_ZEROES, "countLowZeroes", USIZE);
		method(builtin, USIZE_COUNT_HIGH_ZEROES, "countHighZeroes", USIZE);
		method(builtin, USIZE_COUNT_LOW_ONES, "countLowOnes", USIZE);
		method(builtin, USIZE_COUNT_HIGH_ONES, "countHighOnes", USIZE);

		TypeID optionalUSize = registry.getOptional(USIZE);
		getter(builtin, USIZE_HIGHEST_ONE_BIT, "highestOneBit", optionalUSize);
		getter(builtin, USIZE_LOWEST_ONE_BIT, "lowestOneBit", optionalUSize);
		getter(builtin, USIZE_HIGHEST_ZERO_BIT, "highestZeroBit", optionalUSize);
		getter(builtin, USIZE_LOWEST_ZERO_BIT, "lowestZeroBit", optionalUSize);
		getter(builtin, USIZE_BIT_COUNT, "bitCount", USIZE);

		processType(builtin);
	}

	private void visitFloat() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "float", Modifiers.PUBLIC, null);

		neg(builtin, FLOAT_NEG, FLOAT);
		inc(builtin, FLOAT_INC, FLOAT);
		dec(builtin, FLOAT_DEC, FLOAT);

		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, BYTE, FLOAT, BYTE_TO_FLOAT);
		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, SBYTE, FLOAT, SBYTE_TO_FLOAT);
		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, SHORT, FLOAT, SHORT_TO_FLOAT);
		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, USHORT, FLOAT, USHORT_TO_FLOAT);
		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, INT, FLOAT, INT_TO_FLOAT);
		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, UINT, FLOAT, UINT_TO_FLOAT);
		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, LONG, FLOAT, LONG_TO_FLOAT);
		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, ULONG, FLOAT, ULONG_TO_FLOAT);
		addWithCastedOperand(builtin, FLOAT_ADD_FLOAT, USIZE, FLOAT, USIZE_TO_FLOAT);
		add(builtin, FLOAT_ADD_FLOAT, FLOAT, FLOAT);
		add(builtin, DOUBLE_ADD_DOUBLE, DOUBLE, DOUBLE, FLOAT_TO_DOUBLE);

		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, BYTE, FLOAT, BYTE_TO_FLOAT);
		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, SBYTE, FLOAT, SBYTE_TO_FLOAT);
		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, SHORT, FLOAT, SHORT_TO_FLOAT);
		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, USHORT, FLOAT, USHORT_TO_FLOAT);
		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, INT, FLOAT, INT_TO_FLOAT);
		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, UINT, FLOAT, UINT_TO_FLOAT);
		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, LONG, FLOAT, LONG_TO_FLOAT);
		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, ULONG, FLOAT, ULONG_TO_FLOAT);
		subWithCastedOperand(builtin, FLOAT_SUB_FLOAT, USIZE, FLOAT, USIZE_TO_FLOAT);
		sub(builtin, FLOAT_SUB_FLOAT, FLOAT, FLOAT);
		sub(builtin, DOUBLE_SUB_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);

		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, BYTE, FLOAT, BYTE_TO_FLOAT);
		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, SBYTE, FLOAT, SBYTE_TO_FLOAT);
		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, SHORT, FLOAT, SHORT_TO_FLOAT);
		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, USHORT, FLOAT, USHORT_TO_FLOAT);
		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, INT, FLOAT, INT_TO_FLOAT);
		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, UINT, FLOAT, UINT_TO_FLOAT);
		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, LONG, FLOAT, LONG_TO_FLOAT);
		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, ULONG, FLOAT, ULONG_TO_FLOAT);
		mulWithCastedOperand(builtin, FLOAT_MUL_FLOAT, USIZE, FLOAT, USIZE_TO_FLOAT);
		mul(builtin, FLOAT_MUL_FLOAT, FLOAT, FLOAT);
		mul(builtin, DOUBLE_MUL_DOUBLE, DOUBLE, DOUBLE, LONG_TO_DOUBLE);

		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, BYTE, FLOAT, BYTE_TO_FLOAT);
		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, SBYTE, FLOAT, SBYTE_TO_FLOAT);
		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, SHORT, FLOAT, SHORT_TO_FLOAT);
		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, USHORT, FLOAT, USHORT_TO_FLOAT);
		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, INT, FLOAT, INT_TO_FLOAT);
		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, UINT, FLOAT, UINT_TO_FLOAT);
		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, LONG, FLOAT, LONG_TO_FLOAT);
		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, ULONG, FLOAT, ULONG_TO_FLOAT);
		divWithCastedOperand(builtin, FLOAT_DIV_FLOAT, USIZE, FLOAT, USIZE_TO_FLOAT);
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
		castExplicit(builtin, FLOAT_TO_LONG, LONG);
		castExplicit(builtin, FLOAT_TO_ULONG, ULONG);
		castExplicit(builtin, FLOAT_TO_USIZE, USIZE);
		castImplicit(builtin, FLOAT_TO_DOUBLE, DOUBLE);
		castImplicit(builtin, FLOAT_TO_STRING, STRING);

		staticMethod(builtin, FLOAT_PARSE, "parse", FLOAT, STRING);
		staticMethod(builtin, FLOAT_FROM_BITS, "fromBits", FLOAT, UINT);

		getter(builtin, FLOAT_BITS, "bits", UINT);

		processType(builtin);
	}

	private void visitDouble() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "double", Modifiers.PUBLIC, null);

		neg(builtin, DOUBLE_NEG, DOUBLE);
		inc(builtin, DOUBLE_INC, DOUBLE);
		dec(builtin, DOUBLE_DEC, DOUBLE);

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
		castExplicit(builtin, DOUBLE_TO_LONG, LONG);
		castExplicit(builtin, DOUBLE_TO_ULONG, ULONG);
		castExplicit(builtin, DOUBLE_TO_USIZE, USIZE);
		castImplicit(builtin, DOUBLE_TO_FLOAT, FLOAT);
		castImplicit(builtin, DOUBLE_TO_STRING, STRING);

		staticMethod(builtin, DOUBLE_PARSE, "parse", DOUBLE, STRING);
		staticMethod(builtin, DOUBLE_FROM_BITS, "fromBits", DOUBLE, ULONG);

		getter(builtin, DOUBLE_BITS, "bits", ULONG);

		processType(builtin);
	}

	private void visitChar() {
		ClassDefinition builtin = new ClassDefinition(BUILTIN, Module.BUILTIN, null, "char", Modifiers.PUBLIC, null);

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
		castImplicit(builtin, CHAR_TO_USIZE, USIZE);
		castImplicit(builtin, CHAR_TO_STRING, STRING);

		getter(builtin, CHAR_GET_MIN_VALUE, "MIN_VALUE", CHAR);
		getter(builtin, CHAR_GET_MAX_VALUE, "MAX_VALUE", CHAR);

		method(builtin, CHAR_REMOVE_DIACRITICS, "removeDiacritics", CHAR);
		method(builtin, CHAR_TO_LOWER_CASE, "toLowerCase", CHAR);
		method(builtin, CHAR_TO_UPPER_CASE, "toUpperCase", CHAR);

		processType(builtin);
	}

	private void castedTargetCall(OperatorMember member, TypeID toType, BuiltinID casterBuiltin) {
		CasterMemberRef caster = castImplicitRef(member.definition, casterBuiltin, toType);
		TranslatedOperatorMemberRef method = new TranslatedOperatorMemberRef(member, members.type, GenericMapper.EMPTY, call -> member.ref(members.type, null).call(call.position, caster.cast(call.position, call.target, true), call.arguments, call.scope));
		members.getOrCreateGroup(member.operator).addMethod(method, TypeMemberPriority.SPECIFIED);
	}

	private void castedOperandCall(OperatorMember member, TypeID toType, BuiltinID casterBuiltin) {
		CasterMemberRef caster = castImplicitRef(member.definition, casterBuiltin, toType);
		TranslatedOperatorMemberRef method = new TranslatedOperatorMemberRef(member, members.type, GenericMapper.EMPTY,
				call -> member.ref(members.type, null).call(call.position, call.target, new CallArguments(caster.cast(call.position, call.arguments.arguments[0], true)), call.scope));
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
				builtin).ref(members.type, null));
	}

	private void not(HighLevelDefinition cls, BuiltinID id, TypeID result) {
		operator(cls, OperatorType.NOT, new FunctionHeader(result), id);
	}

	private void invert(HighLevelDefinition cls, BuiltinID id, TypeID result) {
		operator(cls, OperatorType.INVERT, new FunctionHeader(result), id);
	}

	private void neg(HighLevelDefinition cls, BuiltinID id, TypeID result) {
		operator(cls, OperatorType.NEG, new FunctionHeader(result), id);
	}

	private void inc(HighLevelDefinition cls, BuiltinID id, TypeID result) {
		operator(cls, OperatorType.INCREMENT, new FunctionHeader(result), id);
	}

	private void dec(HighLevelDefinition cls, BuiltinID id, TypeID result) {
		operator(cls, OperatorType.DECREMENT, new FunctionHeader(result), id);
	}

	private OperatorMember addOp(HighLevelDefinition definition, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				definition,
				Modifiers.PUBLIC,
				OperatorType.ADD,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	////private void add(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
	////	add(definition, id, operand, (TypeID) result);
	////}

	private void add(HighLevelDefinition definition, BuiltinID id, TypeID operand, TypeID result) {
		addOp(definition, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void add(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedTargetCall(addOp(definition, id, operand, result), result, caster);
	}

	private void addWithCastedOperand(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedOperandCall(addOp(definition, id, operand, result), operand, caster);
	}

	private OperatorMember subOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SUB,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void sub(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		subOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void sub(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedTargetCall(subOp(definition, id, operand, result), result, caster);
	}

	private void subWithCastedOperand(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedOperandCall(subOp(definition, id, operand, result), operand, caster);
	}

	private OperatorMember mulOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.MUL,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void mul(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		mulOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void mul(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedTargetCall(mulOp(definition, id, operand, result), result, caster);
	}

	private void mulWithCastedOperand(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedOperandCall(mulOp(definition, id, operand, result), operand, caster);
	}

	private OperatorMember divOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.DIV,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void div(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		divOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void div(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedTargetCall(divOp(definition, id, operand, result), result, caster);
	}

	private void divWithCastedOperand(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedOperandCall(divOp(definition, id, operand, result), operand, caster);
	}

	private OperatorMember modOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.MOD,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void mod(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		modOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void mod(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedTargetCall(modOp(definition, id, operand, result), result, caster);
	}

	private OperatorMember shlOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SHL,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void shl(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		shlOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private OperatorMember shrOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SHR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void shr(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		shrOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private OperatorMember ushrOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.USHR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void ushr(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		ushrOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private OperatorMember orOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.OR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void or(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		orOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void or(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		orOp(definition, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private OperatorMember andOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.AND,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void and(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		andOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void and(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedTargetCall(andOp(definition, id, operand, result), result, caster);
	}

	private OperatorMember xorOp(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.XOR,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id);
	}

	private void xor(HighLevelDefinition cls, BuiltinID id, BasicTypeID operand, BasicTypeID result) {
		xorOp(cls, id, operand, result).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void xor(HighLevelDefinition definition, BuiltinID id, BasicTypeID operand, BasicTypeID result, BuiltinID caster) {
		castedTargetCall(xorOp(definition, id, operand, result), result, caster);
	}

	private void indexGet(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID result) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.INDEXGET,
				new FunctionHeader(result, new FunctionParameter(operand)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void indexSet(HighLevelDefinition cls, BuiltinID id, TypeID operand, TypeID value) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.INDEXSET,
				new FunctionHeader(VOID, operand, value),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private OperatorMember compareOp(HighLevelDefinition cls, BuiltinID id, TypeID operand) {
		return new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.COMPARE,
				new FunctionHeader(INT, new FunctionParameter(operand)),
				id);
	}

	private void compare(HighLevelDefinition cls, BuiltinID id, TypeID operand) {
		compareOp(cls, id, operand).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void compare(HighLevelDefinition definition, BuiltinID id, TypeID operand, BuiltinID caster) {
		castedTargetCall(compareOp(definition, id, operand), operand, caster);
	}

	private void getter(HighLevelDefinition cls, BuiltinID id, String name, TypeID type) {
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
			TypeID... arguments) {
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
			TypeID result,
			TypeID... arguments) {
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
			TypeID result,
			TypeID... arguments) {
		register(new MethodMember(
				BUILTIN,
				cls,
				Modifiers.STATIC | Modifiers.PUBLIC | Modifiers.EXTERN,
				name,
				new FunctionHeader(result, arguments),
				id));
	}

	private void castExplicit(HighLevelDefinition cls, BuiltinID id, TypeID result) {
		new CasterMember(
				CodePosition.BUILTIN,
				cls,
				Modifiers.PUBLIC,
				result,
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void castImplicit(HighLevelDefinition cls, BuiltinID id, TypeID result) {
		new CasterMember(
				CodePosition.BUILTIN,
				cls,
				Modifiers.PUBLIC | Modifiers.IMPLICIT,
				result,
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private CasterMemberRef castImplicitRef(HighLevelDefinition definition, BuiltinID id, TypeID result) {
		return new CasterMemberRef(new CasterMember(
				CodePosition.BUILTIN,
				definition,
				Modifiers.PUBLIC | Modifiers.IMPLICIT,
				result,
				id), members.type, result);
	}

	private void equals(HighLevelDefinition cls, BuiltinID id, TypeID type) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.EQUALS,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void same(HighLevelDefinition cls, BuiltinID id, TypeID type) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.SAME,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void notequals(HighLevelDefinition cls, BuiltinID id, TypeID type) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.NOTEQUALS,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void notsame(HighLevelDefinition cls, BuiltinID id, TypeID type) {
		new OperatorMember(
				BUILTIN,
				cls,
				Modifiers.PUBLIC,
				OperatorType.NOTSAME,
				new FunctionHeader(BOOL, new FunctionParameter(type)),
				id).registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}

	private void iterator(HighLevelDefinition cls, BuiltinID builtin, TypeID... types) {
		new IteratorMember(BUILTIN, cls, Modifiers.PUBLIC, types, registry, builtin)
				.registerTo(members, TypeMemberPriority.SPECIFIED, null);
	}
}
