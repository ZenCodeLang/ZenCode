package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;

public class RangeTypeSymbol implements TypeSymbol {
	public static final TypeParameter PARAMETER = new TypeParameter(CodePosition.BUILTIN, "V");
	public static final RangeTypeSymbol INSTANCE = new RangeTypeSymbol();

	private final TypeParameter[] typeParameters = { PARAMETER };

	private RangeTypeSymbol() {}

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
	}

	@Override
	public String describe() {
		return "Range";
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
		return Modifiers.PUBLIC;
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
		return "Range";
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments) {
		TypeID baseType = typeArguments[0];
		GenericMapper mapper = GenericMapper.single(PARAMETER, baseType);
		RangeTypeID type = new RangeTypeID(baseType);

		MemberSet.Builder members = MemberSet.create();
		members.getter(mapper.map(type, BuiltinMethodSymbol.RANGE_FROM));
		members.getter(mapper.map(type, BuiltinMethodSymbol.RANGE_TO));

		if (baseType == BYTE
				|| baseType == SBYTE
				|| baseType == SHORT
				|| baseType == USHORT
				|| baseType == INT
				|| baseType == UINT
				|| baseType == LONG
				|| baseType == ULONG
				|| baseType == USIZE) {
			members.iterator(mapper.map(type, BuiltinMethodSymbol.ITERATOR_INT_RANGE));
		}

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
