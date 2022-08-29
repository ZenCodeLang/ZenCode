package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;

public class GenericMapTypeSymbol implements TypeSymbol {
	public static final GenericMapTypeSymbol INSTANCE = new GenericMapTypeSymbol();

	private final Modifiers MODIFIERS = Modifiers.PUBLIC;

	public static final TypeParameter PARAMETER = new TypeParameter(CodePosition.BUILTIN, "P");
	public static final TypeParameter VALUE = new TypeParameter(CodePosition.BUILTIN, "V");
	public static final GenericMapTypeID PROTOTYPE = new GenericMapTypeID(new GenericTypeID(PARAMETER), VALUE);

	private final TypeParameter[] TYPE_PARAMETERS = new TypeParameter[] { PARAMETER, VALUE };

	private GenericMapTypeSymbol() {}

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
	}

	@Override
	public String describe() {
		return "generic map";
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
		return "GenericMap";
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments) {
		TypeParameter parameter = ((GenericTypeID)typeArguments[0]).parameter;
		TypeID value = typeArguments[1];
		GenericMapTypeID type = new GenericMapTypeID(value, parameter);

		MemberSet.Builder members = MemberSet.create();
		Map<TypeParameter, TypeID> parameterFilled = new HashMap<>();
		parameterFilled.put(PARAMETER, typeArguments[0]);
		parameterFilled.put(VALUE, value);
		GenericMapper mapper = new GenericMapper(parameterFilled);

		TypeID valueType = mapper.map(value);

		FunctionHeader getOptionalHeader = new FunctionHeader(
				new TypeParameter[]{parameter},
				new OptionalTypeID(valueType),
				null,
				FunctionParameter.NONE);
		FunctionHeader putHeader = new FunctionHeader(new TypeParameter[]{parameter}, VOID, null, new FunctionParameter(valueType));
		FunctionHeader containsHeader = new FunctionHeader(new TypeParameter[]{parameter}, BOOL, null, FunctionParameter.NONE);

		members.constructor(new MethodInstance(BuiltinMethodSymbol.GENERICMAP_CONSTRUCTOR));

		members.method(mapper.map(type, BuiltinMethodSymbol.GENERICMAP_GETOPTIONAL));
		members.method(mapper.map(type, BuiltinMethodSymbol.GENERICMAP_PUT));
		members.method(mapper.map(type, BuiltinMethodSymbol.GENERICMAP_CONTAINS));
		members.method(mapper.map(type, BuiltinMethodSymbol.GENERICMAP_ADDALL));

		members.method(new MethodInstance(BuiltinMethodSymbol.GENERICMAP_SIZE));
		members.method(new MethodInstance(BuiltinMethodSymbol.GENERICMAP_ISEMPTY));
		members.method(new MethodInstance(BuiltinMethodSymbol.GENERICMAP_HASHCODE));

		members.method(mapper.map(type, BuiltinMethodSymbol.GENERICMAP_SAME));
		members.method(mapper.map(type, BuiltinMethodSymbol.GENERICMAP_NOTSAME));

		return members.build();
	}

	@Override
	public TypeParameter[] getTypeParameters() {
		return TYPE_PARAMETERS;
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
