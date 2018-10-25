/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class PrecompilationState implements TypeMemberPreparer {
	private final Map<IDefinitionMember, CompilableMember> members = new HashMap<>();
	private final Set<ParsedDefinitionMember> compilingMembers = new HashSet<>();
	
	public PrecompilationState() {
		
	}
	
	public void register(BaseScope definitionScope, ParsedDefinitionMember member) {
		members.put(member.getCompiled(), new CompilableMember(member, definitionScope));
	}
	
	public boolean precompile(IDefinitionMember member) throws CompileException {
		if (!members.containsKey(member))
			return true;
		
		CompilableMember cMember = members.get(member);
		if (compilingMembers.contains(cMember.member))
			return false;
		
		compilingMembers.add(cMember.member);
		cMember.member.compile(cMember.definitionScope);
		compilingMembers.remove(cMember.member);
		return true;
	}
	
	public void end(ParsedDefinitionMember member) {
		compilingMembers.remove(member);
	}

	@Override
	public void prepare(IDefinitionMember member) throws CompileException {
		precompile(member);
	}
	
	private class CompilableMember {
		private final ParsedDefinitionMember member;
		private final BaseScope definitionScope;
		
		public CompilableMember(ParsedDefinitionMember member, BaseScope definitionScope) {
			this.member = member;
			this.definitionScope = definitionScope;
		}
	}
}
