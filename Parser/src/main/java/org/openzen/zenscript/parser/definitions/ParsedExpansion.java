package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedExpansion extends BaseParsedDefinition {
	public static ParsedExpansion parseExpansion(
			CompilingPackage pkg,
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens,
			HighLevelDefinition outerDefinition) throws ParseException
	{
		List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);
		IParsedType target = IParsedType.parse(tokens);
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedExpansion result = new ParsedExpansion(pkg, position, modifiers, annotations, parameters, target, outerDefinition);
		try {
			while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
				result.addMember(ParsedDefinitionMember.parse(tokens, result, null));
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
			tokens.recoverUntilToken(ZSTokenType.T_ACLOSE);
		}
		return result;
	}
	
	private final List<ParsedTypeParameter> parameters;
	private final IParsedType target;
	private final ExpansionDefinition compiled;
	
	public ParsedExpansion(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, List<ParsedTypeParameter> genericParameters, IParsedType target, HighLevelDefinition outerDefinition) {
		super(position, modifiers, pkg, annotations);
		
		this.parameters = genericParameters;
		this.target = target;
		
		compiled = new ExpansionDefinition(position, pkg.module, pkg.getPackage(), modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(genericParameters));
	}
	
	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void linkTypesLocal(TypeResolutionContext context) {
		ParsedTypeParameter.compile(context, compiled.typeParameters, this.parameters);
		compiled.target = target.compile(context);
		if (compiled.target == null)
			throw new RuntimeException(position + ": Could not compile expansion target: " + target);
		
		super.linkTypesLocal(context);
	}
}
