package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingExpansion;
import org.openzen.zenscript.codemodel.compilation.DefinitionCompiler;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParsedEnum extends BaseParsedDefinition {
	private final List<ParsedEnumConstant> enumValues = new ArrayList<>();
	private final IParsedType asType;
	private final String name;

	public ParsedEnum(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations, String name, IParsedType asType) {
		super(position, modifiers, annotations);

		this.asType = asType;
		this.name = name;
	}

	public static ParsedEnum parseEnum(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		IParsedType asType = null;
		if (tokens.optional(ZSTokenType.K_AS) != null)
			asType = IParsedType.parse(tokens);

		tokens.required(ZSTokenType.T_AOPEN, "{ expected");

		ParsedEnum result = new ParsedEnum(position, modifiers, annotations, name, asType);

		while (!tokens.isNext(ZSTokenType.T_ACLOSE) && !tokens.isNext(ZSTokenType.T_SEMICOLON)) {
			result.addEnumValue(ParsedEnumConstant.parse(tokens, result.enumValues.size()));
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

	public void addEnumValue(ParsedEnumConstant value) {
		enumValues.add(value);
	}

	@Override
	public void registerCompiling(
			List<CompilingDefinition> definitions,
			List<CompilingExpansion> expansions,
			DefinitionCompiler compiler
	) {
		definitions.add(compileAsDefinition(compiler, null));
	}

	@Override
	public CompilingDefinition compileAsDefinition(DefinitionCompiler compiler, HighLevelDefinition outer) {
		CompilingPackage pkg = compiler.getPackage();
		EnumDefinition compiled = new EnumDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outer);
		return new Compiling(compiler, compiled, outer != null);
	}

	private class Compiling extends BaseCompilingDefinition {
		private final EnumDefinition compiled;
		private final List<ParsedEnumConstant.Compiling> enumValues;

		public Compiling(DefinitionCompiler compiler, EnumDefinition compiled, boolean inner) {
			super(ParsedEnum.this, compiler, name, compiled, inner);

			this.compiled = compiled;
			enumValues = ParsedEnum.this.enumValues.stream()
					.map(value -> value.compile(compiled))
					.collect(Collectors.toList());
		}

		@Override
		public void linkTypes() {
			super.linkTypes();

			if (asType != null)
				compiled.asType = asType.compile(compiler.types());

			for (ParsedEnumConstant.Compiling constant : enumValues) {
				compiled.addEnumConstant(constant.compiled);
			}
		}

		@Override
		public void compileMembers(List<CompileException> errors) {
			super.compileMembers(errors);

			TypeID type = compiler.types().definitionOf(compiled, TypeID.NONE);
			ExpressionCompiler expressionCompiler = compiler.forMembers(compiled).forFieldInitializers();
			for (ParsedEnumConstant.Compiling constant : enumValues) {
				constant.compileCode(type, expressionCompiler);
			}
		}
	}
}
