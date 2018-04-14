/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.ImplementationScope;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedImplementation extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final IParsedType type;
	private final List<ParsedDefinitionMember> members;
	
	private ImplementationMember compiled;
	
	public ParsedImplementation(CodePosition position, int modifiers, IParsedType type, List<ParsedDefinitionMember> members) {
		this.position = position;
		this.modifiers = modifiers;
		this.type = type;
		this.members = members;
	}
	
	@Override
	public void linkInnerTypes(HighLevelDefinition definition) {
		
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new ImplementationMember(position, modifiers, type.compile(scope));
		
		for (ParsedDefinitionMember member : members) {
			member.linkTypes(scope);
			compiled.addMember(member.getCompiled());
		}
	}

	@Override
	public ImplementationMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(BaseScope scope) {
		ImplementationScope innerScope = new ImplementationScope(scope, compiled);
		for (ParsedDefinitionMember member : members) {
			member.compile(innerScope);
		}
	}
}
