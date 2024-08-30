package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class MapTypeSymbol implements TypeSymbol {
	private final Modifiers MODIFIERS = Modifiers.PUBLIC;

	public static final TypeParameter KEY_PARAMETER = new TypeParameter(CodePosition.BUILTIN, "K");
	public static final TypeParameter VALUE_PARAMETER = new TypeParameter(CodePosition.BUILTIN, "V");
	public static final GenericTypeID KEY_TYPE = new GenericTypeID(KEY_PARAMETER);
	public static final GenericTypeID VALUE_TYPE = new GenericTypeID(VALUE_PARAMETER);

	public static final MapTypeSymbol INSTANCE = new MapTypeSymbol();

	private final TypeParameter[] typeParameters = { KEY_PARAMETER, VALUE_PARAMETER };

	private MapTypeSymbol() {}

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
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
		return "Map";
	}

	@Override
	public ResolvedType resolve(TypeID type_, TypeID[] typeArguments) {
		GenericMapper mapper = GenericMapper.create(typeParameters, typeArguments);
		AssocTypeID type = (AssocTypeID) type_;

		MemberSet.Builder members = MemberSet.create();

		members.constructor(new MethodInstance(BuiltinMethodSymbol.ASSOC_CONSTRUCTOR));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_INDEXGET));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_INDEXSET));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_GETORDEFAULT));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_CONTAINS));

		members.method(new MethodInstance(BuiltinMethodSymbol.ASSOC_SIZE));
		members.method(new MethodInstance(BuiltinMethodSymbol.ASSOC_ISEMPTY));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_KEYS));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_VALUES));
		members.method(new MethodInstance(BuiltinMethodSymbol.ASSOC_HASHCODE));

		members.iterator(new IteratorInstance(
				type,
				new TypeID[]{type.keyType},
				mapper.map(type, BuiltinMethodSymbol.ITERATOR_ASSOC_KEYS)
		));
		members.iterator(new IteratorInstance(
				type,
				new TypeID[]{type.keyType, type.valueType},
				mapper.map(type, BuiltinMethodSymbol.ITERATOR_ASSOC_KEY_VALUES)
		));

		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_EQUALS));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_NOTEQUALS));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_SAME));
		members.method(mapper.map(type, BuiltinMethodSymbol.ASSOC_NOTSAME));

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
