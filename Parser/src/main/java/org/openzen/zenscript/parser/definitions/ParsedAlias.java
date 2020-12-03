package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.CompilingType;
import org.openzen.zenscript.codemodel.context.LocalTypeResolutionContext;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedAlias extends ParsedDefinition {
	public static ParsedAlias parseAlias(
			CompilingPackage pkg,
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens,
			HighLevelDefinition outerDefinition) throws ParseException
	{
		try {
			String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
			List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);
			tokens.required(ZSTokenType.K_AS, "as expected");
			IParsedType type = IParsedType.parse(tokens);
			tokens.required(ZSTokenType.T_SEMICOLON, "; expected");
			return new ParsedAlias(pkg, position, modifiers, annotations, name, parameters, type, outerDefinition);
		} catch (ParseException ex) {
			tokens.recoverUntilTokenOrNewline(ZSTokenType.T_SEMICOLON);
			throw ex;
		}
	}
	
	private final String name;
	private final List<ParsedTypeParameter> parameters;
	private final IParsedType type;
	
	private final AliasDefinition compiled;
	private boolean typesLinked = false;
	
	public ParsedAlias(
			CompilingPackage pkg,
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			List<ParsedTypeParameter> parameters,
			IParsedType type,
			HighLevelDefinition outerDefinition)
	{
		super(position, modifiers, pkg, annotations);
		
		this.name = name;
		this.parameters = parameters;
		this.type = type;
		
		compiled = new AliasDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outerDefinition);
		
		if (parameters != null && parameters.size() > 0) {
			TypeParameter[] typeParameters = new TypeParameter[parameters.size()];
			for (int i = 0; i < parameters.size(); i++) {
				typeParameters[i] = parameters.get(i).compiled;
			}
			compiled.setTypeParameters(typeParameters);
		}
	}
	
	@Override
	public void linkTypes(TypeResolutionContext context) {
		if (typesLinked)
			return;
		typesLinked = true;
		
		compiled.setType(type.compile(context));
		
		for (int i = 0; i < compiled.typeParameters.length; i++) {
			TypeParameter output = compiled.typeParameters[i];
			ParsedTypeParameter input = this.parameters.get(i);
			for (ParsedGenericBound bound : input.bounds) {
				output.addBound(bound.compile(context));
			}
		}
	}
	
	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}
	
	@Override
	public void registerMembers(BaseScope scope, PrecompilationState state) {
		// nothing to do
	}

	@Override
	public void compile(BaseScope scope) {
		// nothing to do
	}

	@Override
	public CompilingType getCompiling(TypeResolutionContext context) {
		return new Compiling(context);
	}
	
	private class Compiling implements CompilingType {
		private final TypeResolutionContext context;
		
		private Compiling(TypeResolutionContext context) {
			this.context = new LocalTypeResolutionContext(context, this, compiled.typeParameters);
		}
		
		@Override
		public CompilingType getInner(String name) {
			// TODO: this should be possible too
			return null;
		}

		@Override
		public HighLevelDefinition load() {
			linkTypes(context);
			return compiled;
		}
	}
}
