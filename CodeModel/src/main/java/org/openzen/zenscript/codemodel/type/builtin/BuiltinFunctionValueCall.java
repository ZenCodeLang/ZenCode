package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class BuiltinFunctionValueCall implements MethodSymbol {
	private final FunctionTypeSymbol type;
	private final TypeID targetType;
	private final MethodID id;

	public BuiltinFunctionValueCall(FunctionTypeSymbol type) {
		this.type = type;
		targetType = DefinitionTypeID.createThis(type);
		id = MethodID.operator(OperatorType.CALL);
	}

	@Override
	public DefinitionSymbol getDefiningType() {
		return type;
	}

	@Override
	public TypeID getTargetType() {
		return targetType;
	}

	@Override
	public Modifiers getModifiers() {
		return id.isStatic() ? Modifiers.PUBLIC_STATIC : Modifiers.PUBLIC;
	}

	@Override
	public MethodID getID() {
		return id;
	}

	@Override
	public FunctionHeader getHeader() {
		return type.header;
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.empty();
	}
}
