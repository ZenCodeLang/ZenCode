/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ImplementationScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedImplementation extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedAnnotation[] annotations;
	private final IParsedType type;
	private final List<ParsedDefinitionMember> members;
	
	private ImplementationMember compiled;
	
	public ParsedImplementation(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			ParsedAnnotation[] annotations,
			IParsedType type,
			List<ParsedDefinitionMember> members)
	{
		super(definition, annotations);
		
		this.position = position;
		this.modifiers = modifiers;
		this.annotations = annotations;
		this.type = type;
		this.members = members;
	}
	
	@Override
	public void linkInnerTypes() {
		
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new ImplementationMember(position, definition, modifiers, type.compile(scope));
		
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
		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, scope);
		
		ImplementationScope innerScope = new ImplementationScope(scope, compiled);
		for (ParsedDefinitionMember member : members) {
			member.compile(innerScope);
		}
	}
}
