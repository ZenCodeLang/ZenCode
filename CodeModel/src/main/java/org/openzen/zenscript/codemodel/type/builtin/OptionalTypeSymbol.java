package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.OptionalResolvingType;

import java.util.Optional;

public class OptionalTypeSymbol implements TypeSymbol {
	public static final OptionalTypeSymbol INSTANCE = new OptionalTypeSymbol();

	private final Modifiers MODIFIERS = Modifiers.PUBLIC;

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
	}

	@Override
	public String describe() {
		return "Optional";
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
		return "Optional";
	}

	@Override
	public ResolvingType resolve(TypeID[] typeArguments) {
		OptionalTypeID type = new OptionalTypeID(typeArguments[0]);
		ResolvingType baseMembers = typeArguments[0].resolve();
		return new OptionalResolvingType(type, baseMembers);
	}

	@Override
	public TypeParameter[] getTypeParameters() {
		return TypeParameter.NONE;
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
