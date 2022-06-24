package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingExpansion;
import org.openzen.zenscript.codemodel.compilation.DefinitionCompiler;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ParsedAlias extends ParsedDefinition {
	private final String name;
	private final List<ParsedTypeParameter> parameters;
	private final IParsedType type;

	public ParsedAlias(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			List<ParsedTypeParameter> parameters,
			IParsedType type) {
		super(position, modifiers, annotations);

		this.name = name;
		this.parameters = parameters;
		this.type = type;
	}

	public static ParsedAlias parseAlias(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens) throws ParseException {
		try {
			String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
			List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);
			tokens.required(ZSTokenType.K_AS, "as expected");
			IParsedType type = IParsedType.parse(tokens);
			tokens.required(ZSTokenType.T_SEMICOLON, "; expected");
			return new ParsedAlias(position, modifiers, annotations, name, parameters, type);
		} catch (ParseException ex) {
			tokens.recoverUntilTokenOrNewline(ZSTokenType.T_SEMICOLON);
			throw ex;
		}
	}

	@Override
	public void registerCompiling(
			List<CompilingDefinition> definitions,
			List<CompilingExpansion> expansions,
			CompilingPackage pkg,
			DefinitionCompiler compiler,
			CompilingDefinition outer
	) {
		AliasDefinition compiled = new AliasDefinition(position, pkg.module, pkg.getPackage(), name, new Modifiers(modifiers), outer == null ? null : outer.getDefinition());
		if (parameters != null && parameters.size() > 0) {
			TypeParameter[] typeParameters = new TypeParameter[parameters.size()];
			for (int i = 0; i < parameters.size(); i++) {
				typeParameters[i] = parameters.get(i).compiled;
			}
			compiled.setTypeParameters(typeParameters);
		}

		definitions.add(new Compiling(compiler, compiled, outer != null));
	}

	private class Compiling implements CompilingDefinition {
		private final DefinitionCompiler compiler;
		private final AliasDefinition compiled;
		private final boolean inner;

		public Compiling(DefinitionCompiler compiler, AliasDefinition compiled, boolean inner) {
			this.compiler = compiler;
			this.compiled = compiled;
			this.inner = inner;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public TypeSymbol getDefinition() {
			return compiled;
		}

		@Override
		public boolean isInner() {
			return inner;
		}

		@Override
		public void linkTypes() {
			compiled.setType(type.compile(compiler.types()));

			for (int i = 0; i < compiled.typeParameters.length; i++) {
				TypeParameter output = compiled.typeParameters[i];
				ParsedTypeParameter input = ParsedAlias.this.parameters.get(i);
				for (ParsedGenericBound bound : input.bounds) {
					output.addBound(bound.compile(compiler.types()));
				}
			}
		}

		@Override
		public Set<TypeSymbol> getDependencies() {
			Set<TypeSymbol> result = new HashSet<>();
			compiled.type.asDefinition().ifPresent(definition -> result.add(definition.definition));
			return result;
		}

		@Override
		public void prepareMembers(List<CompileException> errors) {

		}

		@Override
		public void compileMembers(List<CompileException> errors) {

		}

		@Override
		public Optional<CompilingDefinition> getInner(String name) {
			return Optional.empty();
		}
	}
}
