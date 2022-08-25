package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingExpansion;
import org.openzen.zenscript.codemodel.compilation.DefinitionCompiler;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.ArrayList;
import java.util.List;

public class ParsedVariant extends BaseParsedDefinition {
	private final List<ParsedTypeParameter> typeParameters;
	private final List<ParsedVariantOption> variants = new ArrayList<>();
	private final String name;

	public ParsedVariant(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> typeParameters) {
		super(position, modifiers, annotations);

		this.typeParameters = typeParameters;
		this.name = name;
	}

	public static ParsedVariant parseVariant(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> typeParameters = ParsedTypeParameter.parseAll(tokens);
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");

		ParsedVariant result = new ParsedVariant(position, modifiers, annotations, name, typeParameters);

		int ordinal = 0;
		while (!tokens.isNext(ZSTokenType.T_ACLOSE) && !tokens.isNext(ZSTokenType.T_SEMICOLON)) {
			CodePosition optionPosition = tokens.getPosition();
			String optionName = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
			List<IParsedType> types = new ArrayList<>();
			if (tokens.optional(ZSTokenType.T_BROPEN) != null) {
				types.add(IParsedType.parse(tokens));
				while (tokens.optional(ZSTokenType.T_COMMA) != null) {
					types.add(IParsedType.parse(tokens));
				}
				tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
			}
			result.addVariant(new ParsedVariantOption(optionPosition, optionName, ordinal++, types));
			if (tokens.optional(ZSTokenType.T_COMMA) == null)
				break;
		}

		if (tokens.optional(ZSTokenType.T_SEMICOLON) != null) {
			try {
				while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
					result.addMember(ParsedDefinitionMember.parse(tokens));
				}
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilOnToken(ZSTokenType.T_ACLOSE);
			}
		} else {
			tokens.required(ZSTokenType.T_ACLOSE, "} expected");
		}
		return result;
	}

	public void addVariant(ParsedVariantOption value) {
		variants.add(value);
	}

	@Override
	public void registerCompiling(
			List<CompilingDefinition> definitions,
			List<CompilingExpansion> expansions,
			DefinitionCompiler compiler
	) {
		Compiling compiling = compileAsDefinition(compiler, null);
		definitions.add(compiling);
		compiling.registerCompiling(definitions);
	}

	@Override
	public Compiling compileAsDefinition(DefinitionCompiler compiler, HighLevelDefinition outer) {
		CompilingPackage pkg = compiler.getPackage();
		VariantDefinition compiled = new VariantDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outer);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(typeParameters));

		return new Compiling(compiler, compiled, outer != null);
	}

	private class Compiling extends BaseCompilingDefinition {
		private final VariantDefinition compiled;

		private Compiling(DefinitionCompiler compiler, VariantDefinition compiled, boolean inner) {
			super(ParsedVariant.this, compiler, name, compiled, inner);

			this.compiled = compiled;
		}

		@Override
		public void linkTypes() {
			ParsedTypeParameter.compile(compiler.types(), compiled.typeParameters, typeParameters);
			for (ParsedVariantOption variant : variants) {
				compiled.options.add(variant.compile(compiled, compiler.types()));
			}

			super.linkTypes();
		}
	}
}
