package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.openzen.zencode.shared.CodePosition.BUILTIN;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.USIZE;

public class ArrayTypeSymbol implements TypeSymbol {
	private final Modifiers MODIFIERS = Modifiers.PUBLIC;

	public static final TypeParameter ELEMENT = new TypeParameter(BUILTIN, "E");
	public static final GenericTypeID ELEMENT_TYPE = new GenericTypeID(ELEMENT);

	public static final ArrayTypeSymbol ARRAY = new ArrayTypeSymbol(1);
	private static final List<ArrayTypeSymbol> types = new ArrayList<>();

	static {
		types.add(ARRAY);
	}

	public static ArrayTypeSymbol get(int dimension) {
		while (types.size() < dimension) {
			types.add(new ArrayTypeSymbol(types.size()));
		}

		return types.get(dimension - 1);
	}

	private final int dimension;
	private final TypeParameter[] parameters;

	private ArrayTypeSymbol(int dimension) {
		this.dimension = dimension;
		this.parameters = new TypeParameter[] { new TypeParameter(CodePosition.BUILTIN, "E") };
	}

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
	}

	@Override
	public String describe() {
		return "array";
	}

	@Override
	public boolean isInterface() {
		return false;
	}

	@Override
	public boolean isExpansion() {
		return false;
	}

	@Override
	public Modifiers getModifiers() {
		return MODIFIERS;
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public String getName() {
		return "Array";
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments) {
		TypeID baseType = typeArguments[0];
		TypeID type = new ArrayTypeID(baseType, dimension);
		GenericMapper mapper = GenericMapper.single(parameters[0], baseType);

		MemberSet.Builder members = MemberSet.create();

		FunctionParameter[] indexGetParameters = new FunctionParameter[dimension];
		for (int i = 0; i < indexGetParameters.length; i++)
			indexGetParameters[i] = new FunctionParameter(USIZE);

		members.indexGet(new MethodInstance(
				BuiltinMethodSymbol.ARRAY_INDEXGET,
				new FunctionHeader(baseType, indexGetParameters),
				type));

		if (dimension == 1) {
			FunctionHeader sliceHeader = new FunctionHeader(type, new FunctionParameter(RangeTypeID.USIZE, "range"));
			members.indexGet(new MethodInstance(
					BuiltinMethodSymbol.ARRAY_INDEXGETRANGE,
					sliceHeader,
					type));

			if (baseType == BYTE)
				members.cast(new MethodInstance(BuiltinMethodSymbol.BYTE_ARRAY_AS_SBYTE_ARRAY));
			if (baseType == SBYTE)
				members.cast(new MethodInstance(BuiltinMethodSymbol.SBYTE_ARRAY_AS_BYTE_ARRAY));
			if (baseType == SHORT)
				members.cast(new MethodInstance(BuiltinMethodSymbol.SHORT_ARRAY_AS_USHORT_ARRAY));
			if (baseType == USHORT)
				members.cast(new MethodInstance(BuiltinMethodSymbol.USHORT_ARRAY_AS_SHORT_ARRAY));
			if (baseType == INT)
				members.cast(new MethodInstance(BuiltinMethodSymbol.INT_ARRAY_AS_UINT_ARRAY));
			if (baseType == UINT)
				members.cast(new MethodInstance(BuiltinMethodSymbol.UINT_ARRAY_AS_INT_ARRAY));
			if (baseType == LONG)
				members.cast(new MethodInstance(BuiltinMethodSymbol.LONG_ARRAY_AS_ULONG_ARRAY));
			if (baseType == ULONG)
				members.cast(new MethodInstance(BuiltinMethodSymbol.ULONG_ARRAY_AS_LONG_ARRAY));
		}

		members.contains(mapper.map(type, BuiltinMethodSymbol.ARRAY_CONTAINS));

		if (baseType.hasDefaultValue()) {
			members.constructor(new MethodInstance(
					BuiltinMethodSymbol.ARRAY_CONSTRUCTOR_SIZED,
					new FunctionHeader(VOID, indexGetParameters),
					type));
		}

		FunctionParameter[] initialValueConstructorParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			initialValueConstructorParameters[i] = new FunctionParameter(USIZE);
		initialValueConstructorParameters[dimension] = new FunctionParameter(baseType);
		FunctionHeader initialValueConstructorHeader = new FunctionHeader(VOID, initialValueConstructorParameters);
		members.constructor(new MethodInstance(BuiltinMethodSymbol.ARRAY_CONSTRUCTOR_INITIAL_VALUE, initialValueConstructorHeader, type));

		FunctionParameter[] lambdaConstructorParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			lambdaConstructorParameters[i] = new FunctionParameter(USIZE);

		FunctionHeader lambdaConstructorFunction = new FunctionHeader(baseType, indexGetParameters);
		lambdaConstructorParameters[dimension] = new FunctionParameter(new FunctionTypeID(lambdaConstructorFunction));
		FunctionHeader lambdaConstructorHeader = new FunctionHeader(VOID, lambdaConstructorParameters);
		members.constructor(new MethodInstance(BuiltinMethodSymbol.ARRAY_CONSTRUCTOR_LAMBDA, lambdaConstructorHeader, type));

		{
			TypeParameter mappedConstructorParameter = new TypeParameter(BUILTIN, "T");
			GenericTypeID mappedConstructorParameterType = new GenericTypeID(mappedConstructorParameter);
			FunctionHeader mappedConstructorHeaderWithoutIndex = new FunctionHeader(baseType, mappedConstructorParameterType);
			FunctionHeader mappedConstructorFunctionWithoutIndex = new FunctionHeader(
					new TypeParameter[]{mappedConstructorParameter},
					VOID,
					null,
					new FunctionParameter(new ArrayTypeID(mappedConstructorParameterType, dimension), "original"),
					new FunctionParameter(new FunctionTypeID(mappedConstructorHeaderWithoutIndex), "projection"));
			members.constructor(new MethodInstance(BuiltinMethodSymbol.ARRAY_CONSTRUCTOR_PROJECTED, mappedConstructorFunctionWithoutIndex, type));
		}

		{
			TypeParameter mappedConstructorParameter = new TypeParameter(BUILTIN, "T");
			GenericTypeID mappedConstructorParameterType = new GenericTypeID(mappedConstructorParameter);

			FunctionParameter[] projectionParameters = new FunctionParameter[dimension + 1];
			for (int i = 0; i < dimension; i++)
				projectionParameters[i] = new FunctionParameter(USIZE);
			projectionParameters[dimension] = new FunctionParameter(mappedConstructorParameterType);

			FunctionHeader mappedConstructorHeaderWithIndex = new FunctionHeader(baseType, projectionParameters);
			FunctionHeader mappedConstructorFunctionWithIndex = new FunctionHeader(
					new TypeParameter[]{mappedConstructorParameter},
					VOID,
					null,
					new FunctionParameter(new ArrayTypeID(mappedConstructorParameterType, dimension), "original"),
					new FunctionParameter(new FunctionTypeID(mappedConstructorHeaderWithIndex), "projection"));
			members.constructor(new MethodInstance(BuiltinMethodSymbol.ARRAY_CONSTRUCTOR_PROJECTED_INDEXED, mappedConstructorFunctionWithIndex, type));
		}

		FunctionParameter[] indexSetParameters = new FunctionParameter[dimension + 1];
		for (int i = 0; i < dimension; i++)
			indexSetParameters[i] = new FunctionParameter(USIZE);
		indexSetParameters[dimension] = new FunctionParameter(baseType);

		FunctionHeader indexSetHeader = new FunctionHeader(VOID, indexSetParameters);
		members.indexSet(new MethodInstance(BuiltinMethodSymbol.ARRAY_INDEXSET, indexSetHeader, type));

		if (dimension == 1) {
			members.getter("length", new MethodInstance(BuiltinMethodSymbol.ARRAY_LENGTH1D));
		} else {
			members.getter("length", new MethodInstance(BuiltinMethodSymbol.ARRAY_LENGTHMD));
		}

		members.getter("isEmpty", new MethodInstance(BuiltinMethodSymbol.ARRAY_ISEMPTY));
		members.getter("hashCode", new MethodInstance(BuiltinMethodSymbol.ARRAY_HASHCODE));
		members.iterator(mapper.map(type, BuiltinMethodSymbol.ITERATOR_ARRAY_VALUES));
		if (dimension == 1) {
			members.iterator(mapper.map(type, BuiltinMethodSymbol.ITERATOR_ARRAY_KEY_VALUES));
		}

		FunctionHeader equalityHeader = new FunctionHeader(BOOL, type);
		members.equals(new MethodInstance(BuiltinMethodSymbol.ARRAY_EQUALS, equalityHeader, type));
		members.notEquals(new MethodInstance(BuiltinMethodSymbol.ARRAY_NOTEQUALS, equalityHeader, type));
		members.same(new MethodInstance(BuiltinMethodSymbol.ARRAY_SAME, equalityHeader, type));
		members.notSame(new MethodInstance(BuiltinMethodSymbol.ARRAY_NOTSAME, equalityHeader, type));

		return members.build();

	}

	@Override
	public TypeParameter[] getTypeParameters() {
		return parameters;
	}

	@Override
	public Optional<TypeSymbol> getOuter() {
		return Optional.empty();
	}

	@Override
	public Optional<TypeID> getSupertype(TypeID[] typeArguments) {
		return Optional.empty();
	}
}
