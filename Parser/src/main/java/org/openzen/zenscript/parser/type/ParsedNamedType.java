package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;

public class ParsedNamedType implements IParsedType {
	public final List<ParsedNamePart> name;
	private final CodePosition position;

	public ParsedNamedType(CodePosition position, List<ParsedNamePart> name) {
		this.position = position;
		this.name = name;
	}

	@Override
	public TypeID compile(TypeBuilder typeBuilder) {
		List<GenericName> genericNames = new ArrayList<>();
		for (ParsedNamePart namePart : name)
			genericNames.add(namePart.compile(typeBuilder));

		return typeBuilder.resolve(position, genericNames)
				.orElseGet(() -> new InvalidTypeID(position, new CompileError(CompileExceptionCode.NO_SUCH_TYPE, "Type not found: " + this)));
	}

	@Override
	public String toString() {
		StringBuilder nameTotal = new StringBuilder();
		for (int i = 0; i < name.size(); i++) {
			if (i > 0)
				nameTotal.append(".");
			nameTotal.append(name.get(i).toString());
		}
		return nameTotal.toString();
	}

	@Override
	public AnnotationDefinition compileAnnotation(BaseScope scope) {
		if (name.size() != 1)
			return null;

		return scope.getAnnotation(name.get(0).name);
	}

	@Override
	public TypeID[] compileTypeArguments(TypeBuilder typeBuilder) {
		ParsedNamePart last = name.get(name.size() - 1);
		return IParsedType.compileTypes(last.typeArguments, typeBuilder);
	}

	public static class ParsedNamePart {
		public final String name;
		public final List<IParsedType> typeArguments;

		public ParsedNamePart(String name, List<IParsedType> genericArguments) {
			this.name = name;
			this.typeArguments = genericArguments;
		}

		private GenericName compile(TypeBuilder typeBuilder) {
			return new GenericName(name, IParsedType.compileTypes(typeArguments, typeBuilder));
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder(name);
			if (typeArguments != null) {
				result.append("<");
				for (int i = 0; i < typeArguments.size(); i++) {
					if (i > 0)
						result.append(", ");
					result.append(typeArguments.get(i).toString());
				}
				result.append(">");
			}
			return result.toString();
		}
	}
}
