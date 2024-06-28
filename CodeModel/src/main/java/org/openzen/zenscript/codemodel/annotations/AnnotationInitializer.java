package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.AnyMethod;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class AnnotationInitializer implements AnyMethod {
	private final FunctionHeader header;

	public AnnotationInitializer(FunctionHeader header) {
		this.header = header;
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Optional<MethodInstance> asMethod() {
		return Optional.empty();
	}

	@Override
	public AnyMethod withGenericArguments(GenericMapper mapper) {
		return new AnnotationInitializer(header.withGenericArguments(mapper));
	}
}
