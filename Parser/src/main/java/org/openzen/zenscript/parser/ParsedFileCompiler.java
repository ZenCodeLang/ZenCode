package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.impl.AbstractTypeBuilder;
import org.openzen.zenscript.codemodel.compilation.impl.compiler.ExpressionCompilerImpl;
import org.openzen.zenscript.codemodel.compilation.impl.compiler.LocalSymbols;
import org.openzen.zenscript.codemodel.compilation.impl.compiler.MemberCompilerImpl;
import org.openzen.zenscript.codemodel.compilation.impl.compiler.StatementCompilerImpl;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;

public class ParsedFileCompiler implements DefinitionCompiler {
	private final CompileContext context;
	private final CompilingPackage pkg;
	private final TypeBuilder localTypeBuilder;
	private final Map<String, TypeSymbol> imports = new HashMap<>();

	public ParsedFileCompiler(CompileContext context, CompilingPackage pkg) {
		this.context = context;
		this.pkg = pkg;
		this.localTypeBuilder = new FileTypeBuilder();
	}

	public void addImport(String name, TypeSymbol type) {
		imports.put(name, type);
	}

	@Override
	public CompilingPackage getPackage() {
		return pkg;
	}

	@Override
	public TypeBuilder types() {
		return localTypeBuilder;
	}

	@Override
	public ResolvedType resolve(TypeID type) {
		return context.resolve(type);
	}

	@Override
	public MemberCompiler forMembers(TypeSymbol definition) {
		TypeID compiled = DefinitionTypeID.createThis(definition);
		return new MemberCompilerImpl(context, this, compiled, localTypeBuilder.withGeneric(definition.getTypeParameters()));
	}
	@Override
	public MemberCompiler forExpansionMembers(TypeID extended, TypeSymbol expansion) {
		return new MemberCompilerImpl(context, this, extended, localTypeBuilder.withGeneric(expansion.getTypeParameters()));
	}

	@Override
	public StatementCompiler forScripts(FunctionHeader scriptHeader) {
		return new StatementCompilerImpl(context, null, localTypeBuilder, scriptHeader, new LocalSymbols(scriptHeader), null);
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

			Optional<TypeID> fromPackage = pkg.getType(name);
			if (fromPackage.isPresent())
				return fromPackage;

			return context.resolve(position, name);
		}

		@Override
		public Optional<AnnotationDefinition> resolveAnnotation(List<GenericName> name) {
			return context.resolveAnnotation(name);
		}

		@Override
		public ExpressionCompiler getDefaultValueCompiler() {
			return new ExpressionCompilerImpl(context, null, localTypeBuilder, null, LocalSymbols.empty(), FunctionHeader.EMPTY);
		}
	}
}
