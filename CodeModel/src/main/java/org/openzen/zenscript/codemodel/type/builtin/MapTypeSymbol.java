package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class MapTypeSymbol implements TypeSymbol {
	public static final TypeParameter KEY_PARAMETER = new TypeParameter(CodePosition.BUILTIN, "K");
	public static final TypeParameter VALUE_PARAMETER = new TypeParameter(CodePosition.BUILTIN, "V");
	public static final GenericTypeID KEY_TYPE = new GenericTypeID(KEY_PARAMETER);
	public static final GenericTypeID VALUE_TYPE = new GenericTypeID(VALUE_PARAMETER);

	public static final MapTypeSymbol INSTANCE = new MapTypeSymbol();

	private final TypeParameter[] typeParameters = { KEY_PARAMETER, VALUE_PARAMETER };

	private MapTypeSymbol() {}

	@Override
	public Module getModule() {
		return Module.BUILTIN;
	}

	@Override
	public String describe() {
		return "Map";
	}

	@Override
	public boolean isInterface() {
		return false;
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
		return "Map";
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments) {
		GenericMapper mapper = GenericMapper.create(typeParameters, typeArguments);

		MemberSet.Builder members = MemberSet.create();

		members.constructor(new MethodInstance(BuiltinMethodSymbol.ASSOC_CONSTRUCTOR));
		members.indexGet(mapper.map(BuiltinMethodSymbol.ASSOC_INDEXGET));
		members.indexSet(mapper.map(BuiltinMethodSymbol.ASSOC_INDEXSET));
		members.method(mapper.map(BuiltinMethodSymbol.ASSOC_GETORDEFAULT));
		members.contains(mapper.map(BuiltinMethodSymbol.ASSOC_CONTAINS));

		members.getter(new MethodInstance(BuiltinMethodSymbol.ASSOC_SIZE));
		members.getter(new MethodInstance(BuiltinMethodSymbol.ASSOC_ISEMPTY));
		members.getter(mapper.map(BuiltinMethodSymbol.ASSOC_KEYS));
		members.getter(mapper.map(BuiltinMethodSymbol.ASSOC_VALUES));
		members.getter(new MethodInstance(BuiltinMethodSymbol.ASSOC_HASHCODE));

		members.iterator(mapper.map(BuiltinMethodSymbol.ITERATOR_ASSOC_KEYS));
		members.iterator(mapper.map(BuiltinMethodSymbol.ITERATOR_ASSOC_KEY_VALUES));

		members.equals(mapper.map(BuiltinMethodSymbol.ASSOC_EQUALS));
		members.notEquals(mapper.map(BuiltinMethodSymbol.ASSOC_NOTEQUALS));
		members.same(mapper.map(BuiltinMethodSymbol.ASSOC_SAME));
		members.notSame(mapper.map(BuiltinMethodSymbol.ASSOC_NOTSAME));

		return members.build();
	}

	@Override
	public TypeParameter[] getTypeParameters() {
		return typeParameters;
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
