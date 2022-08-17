package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.List;
import java.util.Set;

public class ParsedClass extends BaseParsedDefinition {
	private final String name;
	private final List<ParsedTypeParameter> parameters;
	private final IParsedType superclass;

	public ParsedClass(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> parameters, IParsedType superclass) {
		super(position, modifiers, annotations);

		this.name = name;
		this.parameters = parameters;
		this.superclass = superclass;
	}

	public static ParsedClass parseClass(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> genericParameters = ParsedTypeParameter.parseAll(tokens);

		IParsedType superclass = null;
		if (tokens.optional(ZSTokenType.T_COLON) != null) {
			superclass = IParsedType.parse(tokens);
		}

		tokens.required(ZSTokenType.T_AOPEN, "{ expected");

		ParsedClass result = new ParsedClass(position, modifiers, annotations, name, genericParameters, superclass);
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
	public void registerCompiling(
			List<CompilingDefinition> definitions,
			List<CompilingExpansion> expansions,
			CompilingPackage pkg,
			DefinitionCompiler compiler,
			CompilingDefinition outer
	) {
		ClassDefinition compiled = new ClassDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outer == null ? null : outer.getDefinition());
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(parameters));

		Compiling compiling = new Compiling(compiler, compiled, outer != null);
		definitions.add(compiling);
		compiling.registerCompiling(definitions);
	}

	private class Compiling extends BaseCompilingDefinition {
		private final ClassDefinition compiled;

		public Compiling(DefinitionCompiler compiler, ClassDefinition compiled, boolean inner) {
			super(ParsedClass.this, compiler, name, compiled, inner);

			this.compiled = compiled;
			this.compiled.setTypeParameters(ParsedTypeParameter.getCompiled(parameters));
		}

		@Override
		public void linkTypes() {
			ParsedTypeParameter.compile(compiler.types(), compiled.typeParameters, parameters);
			if (superclass != null)
				compiled.setSuperType(superclass.compile(compiler.types()));
			super.linkTypes();
		}

		@Override
		public Set<TypeSymbol> getDependencies() {
			Set<TypeSymbol> result = super.getDependencies();
			if (compiled.getSuperType() != null)
				compiled.getSuperType().asDefinition().ifPresent(type -> result.add(type.definition));

			return result;
		}
	}
}
