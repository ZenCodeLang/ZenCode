package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedExpansion extends BaseParsedDefinition {
	private final List<ParsedTypeParameter> parameters;
	private final IParsedType target;

	public ParsedExpansion(CodePosition position, int modifiers, ParsedAnnotation[] annotations, List<ParsedTypeParameter> genericParameters, IParsedType target) {
		super(position, modifiers, annotations);

		this.parameters = genericParameters;
		this.target = target;
	}

	public static ParsedExpansion parseExpansion(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens) throws ParseException {
		List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);
		IParsedType target = IParsedType.parse(tokens);
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");

		ParsedExpansion result = new ParsedExpansion(position, modifiers, annotations, parameters, target);
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
		ExpansionDefinition compiled = new ExpansionDefinition(position, pkg.module, pkg.getPackage(), modifiers);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(parameters));

		Compiling compiling = new Compiling(compiler, compiled);
		expansions.add(compiling);
		compiling.registerCompiling(definitions);
	}

	private class Compiling implements CompilingExpansion {
		private final DefinitionCompiler compiler;
		private final ExpansionDefinition compiled;
		private final CompilingMember[] members;
		private final Map<String, CompilingDefinition> innerDefinitions = new HashMap<>();

		public Compiling(DefinitionCompiler compiler, ExpansionDefinition compiled) {
			this.compiler = compiler;

			this.compiled = compiled;
			this.compiled.setTypeParameters(ParsedTypeParameter.getCompiled(parameters));

			MemberCompiler memberCompiler = compiler.forMembers(compiled);
			members = ParsedExpansion.this.members.stream()
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
		public ExpansionDefinition getCompiling() {
			return compiled;
		}

		@Override
		public void linkTypes() {
			ParsedTypeParameter.compile(compiler.types(), compiled.typeParameters, ParsedExpansion.this.parameters);
			compiled.target = ParsedExpansion.this.target.compile(compiler.types());
			if (compiled.target == null)
				throw new RuntimeException(position + ": Could not compile expansion target: " + target);
		}

		@Override
		public TypeID getTarget() {
			return compiled.target;
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
	}
}
