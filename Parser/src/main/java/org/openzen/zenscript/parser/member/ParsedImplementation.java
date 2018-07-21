/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ImplementationScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedImplementation extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final IParsedType type;
	private final List<ParsedDefinitionMember> members = new ArrayList<>();
	
	private ImplementationMember compiled;
	
	public ParsedImplementation(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			ParsedAnnotation[] annotations,
			IParsedType type)
	{
		super(definition, annotations);
		
		this.position = position;
		this.modifiers = modifiers;
		this.type = type;
	}
	
	public void addMember(ParsedDefinitionMember member) {
		members.add(member);
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
	public boolean inferHeaders(BaseScope scope, PrecompilationState state) {
		return true; // nothing to do here
	}

	@Override
	public void compile(BaseScope scope, PrecompilationState state) {
		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, scope);
		
		ImplementationScope innerScope = new ImplementationScope(scope, compiled);
		for (ParsedDefinitionMember member : members) {
			member.compile(innerScope, state);
		}
	}
}
