/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.ParsedDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedInnerDefinition extends ParsedDefinitionMember {
	private final ParsedDefinition innerDefinition;
	private final InnerDefinitionMember member;
	
	public ParsedInnerDefinition(HighLevelDefinition outer, ParsedDefinition definition) {
		super(outer);
		
		this.innerDefinition = definition;
		
		member = new InnerDefinitionMember(definition.getPosition(), outer, definition.getModifiers(), definition.getCompiled());
	}
	
	@Override
	public void linkInnerTypes() {
		definition.addMember(member);
		
		this.innerDefinition.linkInnerTypes();
	}

	@Override
	public void linkTypes(BaseScope scope) {
		this.innerDefinition.compileMembers(scope);
	}

	@Override
	public InnerDefinitionMember getCompiled() {
		return member;
	}

	@Override
	public void compile(BaseScope scope) {
		innerDefinition.compileCode(scope);
	}
}
