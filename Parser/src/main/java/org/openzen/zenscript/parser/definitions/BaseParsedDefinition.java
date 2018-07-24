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
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.DefinitionScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.member.ParsedInnerDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class BaseParsedDefinition extends ParsedDefinition {
	protected final List<ParsedDefinitionMember> members = new ArrayList<>();
	private boolean typesCompiled = false;
	
	public BaseParsedDefinition(CodePosition position, int modifiers, ParsedAnnotation[] annotations) {
		super(position, modifiers, annotations);
	}
	
	public void addMember(ParsedDefinitionMember member) {
		members.add(member);
	}
	
	@Override
	public void compileTypes(BaseScope scope) {
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled(), false);
		for (ParsedDefinitionMember member : members) {
			if (member instanceof ParsedInnerDefinition) {
				ParsedInnerDefinition inner = (ParsedInnerDefinition)member;
				innerScope.addInnerType(inner.getCompiled().innerDefinition.name, () -> loadInnerType(innerScope, inner));
			}
		}
		
		doCompileTypes(innerScope);
	}
	
	private void doCompileTypes(DefinitionScope innerScope) {
		if (typesCompiled)
			return;
		
		for (ParsedDefinitionMember member : members)
			member.compileTypes(innerScope);
	}
	
	private HighLevelDefinition loadInnerType(DefinitionScope scope, ParsedInnerDefinition member) {
		member.compileTypes(scope);
		return member.getCompiled().innerDefinition;
	}
	
	@Override
	public void linkInnerTypes() {
		for (ParsedDefinitionMember member : members)
			member.linkInnerTypes();
	}

	@Override
	public void compileMembers(BaseScope scope) {
		getCompiled().annotations = ParsedAnnotation.compileForDefinition(annotations, getCompiled(), scope);
		
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members) {
			member.linkTypes(innerScope);
			getCompiled().addMember(member.getCompiled());
		}
	}
	
	@Override
	public void listMembers(BaseScope scope, PrecompilationState state) {
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members)
			state.register(innerScope, member);
	}
	
	@Override
	public void precompile(BaseScope scope, PrecompilationState state) {
		for (ParsedDefinitionMember member : members)
			state.precompile(member.getCompiled());
	}

	@Override
	public void compileCode(BaseScope scope, PrecompilationState state) {
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members)
			member.compile(innerScope, state);
	}
}
