package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.compilation.impl.AbstractTypeBuilder;
import org.openzen.zenscript.codemodel.compilation.impl.compiler.ExpressionCompilerImpl;
import org.openzen.zenscript.codemodel.compilation.impl.compiler.LocalSymbols;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;

public class CompileContext extends AbstractTypeBuilder implements TypeResolver {
	private final ZSPackage rootPackage;
	private final ZSPackage modulePackage;
	private final List<ExpansionSymbol> expansions;
	private final Map<String, IGlobal> globals;
	private final Map<String, CompilingDefinition> compiling = new HashMap<>();
	private final Map<String, AnnotationDefinition> annotations = new HashMap<>();

	public CompileContext(
			ZSPackage rootPackage,
			ZSPackage modulePackage,
			List<ExpansionSymbol> expansions,
			Map<String, IGlobal> globals,
			List<AnnotationDefinition> annotations
	) {
		this.rootPackage = rootPackage;
		this.modulePackage = modulePackage;
		this.expansions = expansions;
		this.globals = globals;

		for (AnnotationDefinition annotation : annotations) {
			this.annotations.put(annotation.getAnnotationName(), annotation);
		}
	}

	/**
	 * Creates an expression compiler without further context.
	 *
	 * @return expression compiler
	 */
	public ExpressionCompiler createStaticCompiler() {
		return new ExpressionCompilerImpl(this,null, this, null, LocalSymbols.empty(), null);
	}

	/**
	 * Creates an expression compiler for the given context.
	 *
	 * @param localType local type (can be null)
	 * @param thrownType local thrown type (can be null)
	 * @param locals locals
	 * @param header local function header (can be null)
	 * @return expression compiler suited for the local context
	 */
	public ExpressionCompiler createExpressionCompiler(LocalType localType, TypeID thrownType, LocalSymbols locals, FunctionHeader header) {
		return new ExpressionCompilerImpl(this, localType, this, thrownType, locals, header);
	}

	public void addCompiling(CompilingDefinition definition) {
		compiling.put(definition.getName(), definition);
	}

	public void addExpansion(ExpansionDefinition expansion) {
		expansions.add(expansion);
	}

	public Optional<IGlobal> findGlobal(String name) {
		return Optional.ofNullable(globals.get(name));
	}

	@Override
	public ResolvedType resolve(TypeID type) {
		return type.resolve().withExpansions(expansions);
	}

	@Override
	public Optional<TypeID> resolve(CodePosition position, List<GenericName> name) {
		if (compiling.containsKey(name.get(0).name)) {
			CompilingDefinition definition = compiling.get(name.get(0).name);
			for (int i = 1; i < name.size(); i++) {
				Optional<CompilingDefinition> inner = definition.getInner(name.get(i).name);
				if (inner.isPresent()) {
					definition = inner.get();
				} else {
					return Optional.empty();
				}
			}
			return Optional.of(DefinitionTypeID.create(definition.getDefinition(), name.get(name.size() - 1).arguments));
		} else if (rootPackage.contains(name.get(0).name)) {
			return rootPackage.getType(name, expansions);
		}

		return Optional.ofNullable(globals.get(name.get(0).name))
				.flatMap(t -> t.getType(position, this, name.get(0).arguments))
				.flatMap(t -> {
					for (int i = 1; i < name.size(); i++) {
						Optional<TypeSymbol> inner = t.resolveWithoutExpansions().findInnerType(name.get(i).name);
						if (inner.isPresent()) {
							t = DefinitionTypeID.create(inner.get(), name.get(i).arguments);
						} else {
							return Optional.empty();
						}
					}
					return Optional.of(t);
				});
	}

	@Override
	public Optional<AnnotationDefinition> resolveAnnotation(List<GenericName> name) {
		if (name.size() > 1) {
			return Optional.empty();
		} else {
			return Optional.ofNullable(annotations.get(name.get(0).name));
		}
	}

	@Override
	public ExpressionCompiler getDefaultValueCompiler() {
		return createStaticCompiler();
	}
}
