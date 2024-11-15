package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class OptionalIteratorMethod implements MethodSymbol {
	public final MethodSymbol original;

	public OptionalIteratorMethod(MethodSymbol original) {
		this.original = original;
	}

	@Override
	public DefinitionSymbol getDefiningType() {
		return original.getDefiningType();
	}

	@Override
	public TypeID getTargetType() {
		return new OptionalTypeID(original.getTargetType());
	}

	@Override
	public Modifiers getModifiers() {
		return original.getModifiers();
	}

	@Override
	public MethodID getID() {
		return original.getID();
	}

	@Override
	public FunctionHeader getHeader() {
		return original.getHeader();
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return original.getOverrides();
	}
}
