/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.FunctionScope;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class ParsedFunctionalMember extends ParsedDefinitionMember {
	protected final CodePosition position;
	protected final int modifiers;
	protected final ParsedFunctionBody body;
	
	protected FunctionalMember compiled;
	
	public ParsedFunctionalMember(CodePosition position, int modifiers, ParsedFunctionBody body) {
		this.position = position;
		this.modifiers = modifiers;
		this.body = body;
	}
	
	@Override
	public void linkInnerTypes(HighLevelDefinition definition) {
		
	}

	@Override
	public FunctionalMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(BaseScope scope) {
		FunctionScope innerScope = new FunctionScope(scope, compiled.header);
		compiled.setBody(body.compile(innerScope, compiled.header));
	}
}
