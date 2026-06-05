package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.IteratorSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class JavaRuntimeIterator implements IteratorSymbol {
	private final JavaRuntimeMethod method;
	private final TypeID iteratorType;

	public JavaRuntimeIterator(JavaRuntimeMethod method) {
		this.method = method;

		TypeID methodReturnType = method.getHeader().getReturnType();
		iteratorType = methodReturnType
				.asDefinition()
				.map(d -> d.typeArguments[0])
				.orElseThrow(() -> new IllegalArgumentException("Iterator method must return an iterator type"));
	}

	@Override
	public Kind getKind() {
		return Kind.ITERABLE;
	}

	@Override
	public TypeID[] getReturnedTypes(TypeID targetType) {
		return new TypeID[]{iteratorType};
	}

	@Override
	public DefinitionSymbol getDefiningType() {
		return method.getDefiningType();
	}

	@Override
	public TypeID getTargetType() {
		return method.getTargetType();
	}

	@Override
	public Modifiers getModifiers() {
		return method.getModifiers();
	}

	@Override
	public MethodID getID() {
		return method.getID();
	}

	@Override
	public FunctionHeader getHeader() {
		return method.getHeader();
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return method.getOverrides();
	}
}
