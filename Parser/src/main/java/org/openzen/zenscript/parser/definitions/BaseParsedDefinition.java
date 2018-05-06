/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.DefinitionScope;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class BaseParsedDefinition extends ParsedDefinition {
	protected final List<ParsedDefinitionMember> members = new ArrayList<>();
	
	public BaseParsedDefinition(CodePosition position, int modifiers) {
		super(position, modifiers);
	}
	
	public void addMember(ParsedDefinitionMember member) {
		members.add(member);
	}
	
	@Override
	public void linkInnerTypes() {
		for (ParsedDefinitionMember member : members)
			member.linkInnerTypes();
	}

	@Override
	public void compileMembers(BaseScope scope) {
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members) {
			member.linkTypes(innerScope);
			getCompiled().addMember(member.getCompiled());
		}
	}

	@Override
	public void compileCode(BaseScope scope) {
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members)
			member.compile(innerScope);
	}
}
