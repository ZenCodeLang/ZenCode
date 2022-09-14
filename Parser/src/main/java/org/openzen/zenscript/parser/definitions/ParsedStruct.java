package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingExpansion;
import org.openzen.zenscript.codemodel.compilation.DefinitionCompiler;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

import java.util.List;

public class ParsedStruct extends BaseParsedDefinition {
	private final List<ParsedTypeParameter> parameters;
	private final String name;

	public ParsedStruct(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> genericParameters) {
		super(position, modifiers, annotations);

		this.parameters = genericParameters;
		this.name = name;
	}

	public static ParsedStruct parseStruct(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens
	) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);

		tokens.required(ZSTokenType.T_AOPEN, "{");

		ParsedStruct result = new ParsedStruct(position, modifiers, annotations, name, parameters);
		try {
			while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
				result.addMember(ParsedDefinitionMember.parse(tokens));
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
			tokens.recoverUntilOnToken(ZSTokenType.T_ACLOSE);
		}
		return result;
	}

	@Override
	public void registerCompiling(List<CompilingDefinition> definitions, List<CompilingExpansion> expansions, DefinitionCompiler compiler) {
		Compiling compiling = compileAsDefinition(compiler, null);
		definitions.add(compiling);
		compiling.registerCompiling(definitions);
	}

	@Override
	public Compiling compileAsDefinition(DefinitionCompiler compiler, HighLevelDefinition outer) {
		CompilingPackage pkg = compiler.getPackage();
		StructDefinition compiled = new StructDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outer);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(parameters));
		return new Compiling(compiler, compiled, outer != null);
	}

	private class Compiling extends BaseCompilingDefinition<StructDefinition> {
		public Compiling(DefinitionCompiler compiler, StructDefinition compiled, boolean inner) {
			super(ParsedStruct.this, compiler, name, compiled, inner);

			this.compiled.setTypeParameters(ParsedTypeParameter.getCompiled(parameters));
		}

		@Override
		public void linkTypes() {
			ParsedTypeParameter.compile(compiler.types(), compiled.typeParameters, parameters);
			super.linkTypes();
		}
	}
}
