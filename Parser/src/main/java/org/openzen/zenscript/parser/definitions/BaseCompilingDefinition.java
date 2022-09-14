package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.DefinitionCompiler;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;

import java.util.*;

public class BaseCompilingDefinition<T extends HighLevelDefinition> implements CompilingDefinition {
	protected final DefinitionCompiler compiler;
	private final String name;
	private final CompilingMember[] members;
	protected final T compiled;
	private final boolean inner;
	private final Map<String, CompilingDefinition> innerDefinitions = new HashMap<>();

	public BaseCompilingDefinition(
			BaseParsedDefinition parsedDefinition,
			DefinitionCompiler compiler,
			String name,
			T compiled,
			boolean isInner
	) {
		this.compiler = compiler;
		this.name = name;
		this.compiled = compiled;
		this.inner = isInner;

		MemberCompiler memberCompiler = compiler.forMembers(compiled);
		members = parsedDefinition.members.stream()
				.map(member -> member.compile(compiled, null, memberCompiler))
				.toArray(CompilingMember[]::new);

		for (CompilingMember member : members) {
			member.asInner().ifPresent(inner -> innerDefinitions.put(inner.getName(), inner));
		}
	}

	public void registerCompiling(List<CompilingDefinition> definitions) {
		definitions.addAll(innerDefinitions.values());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public HighLevelDefinition getDefinition() {
		return compiled;
	}

	@Override
	public boolean isInner() {
		return inner;
	}

	@Override
	public void linkTypes() {
		for (CompilingMember member : members) {
			member.linkTypes();
			compiled.addMember(member.getCompiled());
		}
	}

	@Override
	public Set<TypeSymbol> getDependencies() {
		Set<TypeSymbol> result = new HashSet<>();
		for (CompilingMember member : members) {
			member.listCompilationOrderDependencies(result);
		}
		return result;
	}

	@Override
	public void prepareMembers(List<CompileException> errors) {
		for (CompilingMember member : members) {
			member.prepare(errors);
		}
	}

	@Override
	public void compileMembers(List<CompileException> errors) {
		for (CompilingMember member : members) {
			member.compile(errors);
		}
	}

	@Override
	public Optional<CompilingDefinition> getInner(String name) {
		return Optional.ofNullable(innerDefinitions.get(name));
	}
}
