/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedGetter extends ParsedFunctionalMember {
	private final String name;
	private final IParsedType type;
	private GetterMember compiled;
	
	public ParsedGetter(
			CodePosition position,
			HighLevelDefinition definition,
			ParsedImplementation implementation,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			ParsedFunctionBody body)
	{
		super(position, definition, implementation, modifiers, annotations, body);
		
		this.name = name;
		this.type = type;
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		compiled = new GetterMember(position, definition, modifiers, name, type.compile(context), null);
	}

	@Override
	public FunctionalMember getCompiled() {
		return compiled;
	}
	
	@Override
	protected void inferHeaders(BaseScope scope) {
		super.inferHeaders(scope);
		
		if (type == ParsedTypeBasic.UNDETERMINED)
			compiled.type = compiled.header.returnType;
	}

	@Override
	protected void fillOverride(TypeScope scope, ITypeID baseType) {
		compiled.setOverrides(scope.getTypeMembers(baseType).getOrCreateGroup(name, false).getGetter());
	}
}
