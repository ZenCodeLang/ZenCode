/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedEnum extends BaseParsedDefinition {
	public static ParsedEnum parseEnum(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedEnum result = new ParsedEnum(pkg, position, modifiers, annotations, name, outerDefinition);
		
		while (!tokens.isNext(ZSTokenType.T_ACLOSE) && !tokens.isNext(ZSTokenType.T_SEMICOLON)) {
			result.addEnumValue(ParsedEnumConstant.parse(tokens, result.compiled, result.enumValues.size()));
			if (tokens.optional(ZSTokenType.T_COMMA) == null)
				break;
		}
		
		if (tokens.optional(ZSTokenType.T_SEMICOLON) != null) {
			while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
				result.addMember(ParsedDefinitionMember.parse(tokens, result, null));
			}
		} else {
			tokens.required(ZSTokenType.T_ACLOSE, "} expected");
		}
		return result;
	}
	
	private final List<ParsedEnumConstant> enumValues = new ArrayList<>();
	
	private final EnumDefinition compiled;
	
	public ParsedEnum(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, HighLevelDefinition outerDefinition) {
		super(position, modifiers, pkg, annotations);
		
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
		for (ParsedEnumConstant constant : enumValues) {
			compiled.addEnumConstant(constant.getCompiled());
		}
		
		super.linkTypesLocal(context);
	}

	@Override
	public void compile(BaseScope scope) {
		super.compile(scope);
		
		DefinitionTypeID type = scope.getTypeRegistry().getForDefinition(compiled, BorrowStorageTag.INVOCATION, ITypeID.NONE);
		ExpressionScope evalScope = new ExpressionScope(scope);
		for (ParsedEnumConstant constant : enumValues) {
			constant.compileCode(type, evalScope);
		}
	}
}
