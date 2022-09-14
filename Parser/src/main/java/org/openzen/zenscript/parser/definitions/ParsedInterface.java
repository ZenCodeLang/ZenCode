package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingExpansion;
import org.openzen.zenscript.codemodel.compilation.DefinitionCompiler;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ParsedInterface extends BaseParsedDefinition {
	private final List<ParsedTypeParameter> typeParameters;
	private final List<IParsedType> superInterfaces;
	private final String name;

	public ParsedInterface(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> typeParameters, List<IParsedType> superInterfaces) {
		super(position, modifiers, annotations);

		this.name = name;
		this.typeParameters = typeParameters;
		this.superInterfaces = superInterfaces;
	}

	public static ParsedInterface parseInterface(
			CodePosition position,
			Modifiers modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> genericParameters = ParsedTypeParameter.parseAll(tokens);
		List<IParsedType> superInterfaces = Collections.emptyList();
		try {
			if (tokens.optional(ZSTokenType.T_COLON) != null) {
				superInterfaces = new ArrayList<>();
				do {
					superInterfaces.add(IParsedType.parse(tokens));
				} while (tokens.optional(ZSTokenType.T_COMMA) != null);
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
		}

		ParsedInterface result = new ParsedInterface(position, modifiers, annotations, name, genericParameters, superInterfaces);

		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		try {
			while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
				result.addMember(ParsedDefinitionMember.parse(tokens));
			}
		} catch (ParseException ex) {
			tokens.recoverUntilOnToken(ZSTokenType.T_ACLOSE);
		}
		return result;
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
		InterfaceDefinition compiled = new InterfaceDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outer);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(typeParameters));
		return new Compiling(compiler, compiled, outer != null);
	}

	private class Compiling extends BaseCompilingDefinition<InterfaceDefinition> {

		public Compiling(DefinitionCompiler compiler, InterfaceDefinition compiled, boolean inner) {
			super(ParsedInterface.this, compiler, name, compiled, inner);

			compiled.setTypeParameters(ParsedTypeParameter.getCompiled(typeParameters));
		}

		@Override
		public void linkTypes() {
			ParsedTypeParameter.compile(compiler.types(), compiled.typeParameters, typeParameters);
			for (IParsedType type : superInterfaces)
				compiled.baseInterfaces.add(type.compile(compiler.types()));

			super.linkTypes();
		}

		@Override
		public Set<TypeSymbol> getDependencies() {
			Set<TypeSymbol> result = super.getDependencies();
			for (TypeID type : compiled.baseInterfaces)
				type.asDefinition().ifPresent(t -> result.add(t.definition));

			return result;
		}
	}
}
