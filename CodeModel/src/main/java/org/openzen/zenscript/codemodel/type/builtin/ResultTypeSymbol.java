package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class ResultTypeSymbol implements TypeSymbol {
	public static final ResultTypeSymbol INSTANCE = new ResultTypeSymbol();

	private final TypeParameter[] typeParameters;

	private ResultTypeSymbol() {
		typeParameters = new TypeParameter[] {
				new TypeParameter(CodePosition.BUILTIN, "V"),
				new TypeParameter(CodePosition.BUILTIN, "E")
		};
	}

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
	}

	@Override
	public String describe() {
		return "Result";
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
		return "Result";
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments) {
		MemberSet members = new MemberSet();
		// TODO
		return members;
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
