package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class FunctionTypeSymbol implements TypeSymbol {
	public static final FunctionTypeSymbol PLACEHOLDER = new FunctionTypeSymbol(FunctionHeader.PLACEHOLDER);
	
	public final FunctionHeader header;
	private final BuiltinFunctionValueCall caller;

	public FunctionTypeSymbol(FunctionHeader header) {
		this.header = header;
		caller = new BuiltinFunctionValueCall(this);
	}

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
	}

	@Override
	public String describe() {
		return new FunctionTypeID(header).toString();
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
		return null;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public String getName() {
		return "function";
	}

	@Override
	public ResolvedType resolve(TypeID type, TypeID[] typeArguments) {
		return MemberSet.create().method(new MethodInstance(caller)).build();
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
