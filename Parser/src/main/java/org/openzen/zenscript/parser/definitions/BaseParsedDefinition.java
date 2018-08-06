/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingType;
import org.openzen.zenscript.codemodel.context.LocalTypeResolutionContext;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.DefinitionScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class BaseParsedDefinition extends ParsedDefinition {
	protected final List<ParsedDefinitionMember> members = new ArrayList<>();
	private boolean typesCompiled = false;
	private final Map<String, CompilingType> innerTypes = new HashMap<>();
	private boolean isCompiled = false;
	
	public BaseParsedDefinition(CodePosition position, int modifiers, ParsedAnnotation[] annotations) {
		super(position, modifiers, annotations);
	}
	
	public void addMember(ParsedDefinitionMember member) {
		members.add(member);
		member.registerInnerTypes(innerTypes);
	}
	
	@Override
	public void linkTypes(TypeResolutionContext context) {
		if (typesCompiled)
			return;
		typesCompiled = true;
		
		System.out.println("compileTypes " + getCompiled().name);
		LocalTypeResolutionContext localContext = new LocalTypeResolutionContext(context, this, getCompiled().genericParameters);
		linkTypesLocal(localContext);
	}
	
	protected void linkTypesLocal(TypeResolutionContext localContext) {
		for (ParsedDefinitionMember member : members) {
			member.linkTypes(localContext);
			getCompiled().addMember(member.getCompiled());
		}
	}
	
	@Override
	public void registerMembers(BaseScope scope, PrecompilationState state) {
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members) {
			state.register(innerScope, member);
			member.registerMembers(innerScope, state);
		}
	}

	@Override
	public void compile(BaseScope scope) {
		if (isCompiled)
			return;
		isCompiled = true;
		
		getCompiled().annotations = ParsedAnnotation.compileForDefinition(annotations, getCompiled(), scope);
		
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members)
			member.compile(innerScope);
	}
	
	@Override
	public HighLevelDefinition load(TypeResolutionContext context) {
		linkTypes(context);
		return getCompiled();
	}
	
	@Override
	public CompilingType getInner(String name) {
		return innerTypes.get(name);
	}
}
