package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedEnum extends BaseParsedDefinition {
	public static ParsedEnum parseEnum(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens, HighLevelDefinition outerDefinition) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		IParsedType asType = null;
		if (tokens.optional(ZSTokenType.K_AS) != null)
			asType = IParsedType.parse(tokens);
		
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedEnum result = new ParsedEnum(pkg, position, modifiers, annotations, name, outerDefinition, asType);
		
		while (!tokens.isNext(ZSTokenType.T_ACLOSE) && !tokens.isNext(ZSTokenType.T_SEMICOLON)) {
			result.addEnumValue(ParsedEnumConstant.parse(tokens, result.compiled, result.enumValues.size()));
			if (tokens.optional(ZSTokenType.T_COMMA) == null)
				break;
		}
		
		if (tokens.optional(ZSTokenType.T_SEMICOLON) != null) {
			try {
				while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
					result.addMember(ParsedDefinitionMember.parse(tokens, result, null));
				}
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilToken(ZSTokenType.T_ACLOSE);
			}
		} else {
			tokens.required(ZSTokenType.T_ACLOSE, "} expected");
		}
		return result;
	}
	
	private final List<ParsedEnumConstant> enumValues = new ArrayList<>();
	
	private final IParsedType asType;
	private final EnumDefinition compiled;
	
	public ParsedEnum(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, HighLevelDefinition outerDefinition, IParsedType asType) {
		super(position, modifiers, pkg, annotations);
		
		this.asType = asType;
		compiled = new EnumDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outerDefinition);
	}
	
	public void addEnumValue(ParsedEnumConstant value) {
		enumValues.add(value);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}
	
	@Override
	protected void linkTypesLocal(TypeResolutionContext context) {
		if (asType != null)
			compiled.asType = asType.compile(context);
		
		for (ParsedEnumConstant constant : enumValues) {
			compiled.addEnumConstant(constant.getCompiled());
		}
		
		super.linkTypesLocal(context);
	}

	@Override
	public void compile(BaseScope scope) throws CompileException {
		super.compile(scope);
		
		DefinitionTypeID type = scope.getTypeRegistry().getForDefinition(compiled, TypeID.NONE);
		ExpressionScope evalScope = new ExpressionScope(scope);
		for (ParsedEnumConstant constant : enumValues) {
			constant.compileCode(type, evalScope);
		}
	}
}
