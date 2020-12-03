package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedClass extends BaseParsedDefinition {
	public static ParsedClass parseClass(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens, HighLevelDefinition outerDefinition) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> genericParameters = ParsedTypeParameter.parseAll(tokens);
		
		IParsedType superclass = null;
		if (tokens.optional(ZSTokenType.T_COLON) != null) {
			superclass = IParsedType.parse(tokens);
		}
		
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedClass result = new ParsedClass(pkg, position, modifiers, annotations, name, genericParameters, superclass, outerDefinition);
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
	private final IParsedType superclass;
	
	private final ClassDefinition compiled;
	
	public ParsedClass(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> parameters, IParsedType superclass, HighLevelDefinition outerDefinition) {
		super(position, modifiers, pkg, annotations);
		
		this.parameters = parameters;
		this.superclass = superclass;
		
		compiled = new ClassDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(parameters));
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	protected void linkTypesLocal(TypeResolutionContext context) {
		ParsedTypeParameter.compile(context, compiled.typeParameters, this.parameters);
		
		if (superclass != null)
			compiled.setSuperType(superclass.compile(context));
		
		super.linkTypesLocal(context);
	}
}
