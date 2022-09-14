package org.openzen.zenscript.codemodel.identifiers.instances;

import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class IteratorInstance {
	public final TypeID targetType;
	public final MethodInstance method;
	private final TypeID[] loopVariableTypes;

	public IteratorInstance(TypeID targetType, TypeID[] loopVariableTypes, MethodInstance method) {
		this.targetType = targetType;
		this.loopVariableTypes = loopVariableTypes;
		this.method = method;
	}

	public int getLoopVariableCount() {
		return loopVariableTypes.length;
	}

	public Optional<MethodInstance> getMethod() {
		return Optional.of(method);
	}

	public TypeID[] getLoopVariableTypes() {
		return loopVariableTypes;
	}
}
