/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.FunctionScope;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStaticInitializer extends ParsedDefinitionMember {
	private final ParsedStatement body;
	
	private final StaticInitializerMember compiled;
	
	public ParsedStaticInitializer(HighLevelDefinition definition, CodePosition position, ParsedStatement body) {
		super(definition);
		
		this.body = body;
		compiled = new StaticInitializerMember(position);
	}

	@Override
	public void linkInnerTypes() {
		
	}

	@Override
	public void linkTypes(BaseScope scope) {
		
	}

	@Override
	public IDefinitionMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(BaseScope scope) {
		compiled.body = body.compile(new FunctionScope(scope, new FunctionHeader(BasicTypeID.VOID)));
	}
}
