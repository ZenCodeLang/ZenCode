package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.CompileContext;
import org.openzen.zenscript.codemodel.compilation.DefinitionCompiler;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.compilation.impl.AbstractTypeBuilder;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParsedFileCompiler implements DefinitionCompiler {
	private final CompileContext context;
	private final TypeBuilder localTypeBuilder;
	private final Map<String, TypeSymbol> imports = new HashMap<>();

	public ParsedFileCompiler(CompileContext context) {
		this.context = context;
		this.localTypeBuilder = new FileTypeBuilder();
	}

	public void addImport(String name, TypeSymbol type) {
		imports.put(name, type);
	}

	@Override
	public TypeBuilder types() {
		return localTypeBuilder;
	}

	@Override
	public ResolvedType resolve(TypeID type) {
		return context.resolve(type);
	}

	private class FileTypeBuilder extends AbstractTypeBuilder {
		public FileTypeBuilder() {
			super();
		}

		@Override
		public Optional<TypeID> resolve(CodePosition position, List<GenericName> name) {
			if (imports.containsKey(name.get(0).name)) {
				TypeID type = DefinitionTypeID.create(imports.get(name.get(0).name), name.get(0).arguments);
				for (int i = 1; i < name.size(); i++) {
					Optional<TypeSymbol> inner = type.resolve().findInnerType(name.get(i).name);
					if (inner.isPresent()) {
						type = DefinitionTypeID.create(inner.get(), name.get(i).arguments);
					} else {
						break;
					}
				}
				return Optional.of(type);
			}
			return context.resolve(position, name);
		}
	}
}
