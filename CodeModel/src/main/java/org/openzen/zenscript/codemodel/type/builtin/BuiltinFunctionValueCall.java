package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

import java.util.Optional;

public class BuiltinFunctionValueCall implements MethodSymbol {
	private final FunctionTypeSymbol type;

	public BuiltinFunctionValueCall(FunctionTypeSymbol type) {
		this.type = type;
	}

	@Override
	public DefinitionSymbol getDefiningType() {
		return type;
	}

	@Override
	public TypeSymbol getTargetType() {
		return type;
	}

	@Override
	public Modifiers getModifiers() {
		return Modifiers.PUBLIC_STATIC;
	}

	@Override
	public String getName() {
		return "()";
	}

	@Override
	public Optional<OperatorType> getOperator() {
		return Optional.of(OperatorType.CALL);
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
