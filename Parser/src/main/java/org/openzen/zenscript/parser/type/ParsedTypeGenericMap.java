package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.TypeBuilder;
import org.openzen.zenscript.parser.definitions.ParsedTypeParameter;

import java.util.List;

public class ParsedTypeGenericMap implements IParsedType {
	private final ParsedTypeParameter key;
	private final IParsedType value;

	public ParsedTypeGenericMap(ParsedTypeParameter key, IParsedType value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		TypeParameter cKey = key.compiled;

		TypeBuilder.GenericMapTypeBuilder builder = typeBuilder.withGeneric(cKey);
		TypeID valueType = this.value.compile(builder);
		return builder.ofValue(valueType);
	}

	private class GenericMapScope implements TypeResolutionContext {
		private final TypeResolutionContext outer;
		private final TypeParameter parameter;

		public GenericMapScope(TypeResolutionContext outer, TypeParameter parameter) {
			this.outer = outer;
			this.parameter = parameter;
		}

		@Override
		public ZSPackage getRootPackage() {
			return outer.getRootPackage();
		}

		@Override
		public GlobalTypeRegistry getTypeRegistry() {
			return outer.getTypeRegistry();
		}

		@Override
		public TypeID getType(CodePosition position, List<GenericName> name) {
			if (name.get(0).name.equals(parameter.name) && name.size() == 1 && name.get(0).hasNoArguments())
				return getTypeRegistry().getGeneric(parameter);

			return outer.getType(position, name);
		}

		@Override
		public TypeID getThisType() {
			return outer.getThisType();
		}

		@Override
		public AnnotationDefinition getAnnotation(String name) {
			return outer.getAnnotation(name);
		}
	}
}
