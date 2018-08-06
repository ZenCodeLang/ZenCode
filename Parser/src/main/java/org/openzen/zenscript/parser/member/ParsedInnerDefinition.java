/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingType;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedInnerDefinition extends ParsedDefinitionMember {
	private final ParsedDefinition innerDefinition;
	private final InnerDefinitionMember member;
	private boolean typesCompiled = false;
	
	public ParsedInnerDefinition(HighLevelDefinition outer, ParsedDefinition definition) {
		super(outer, ParsedAnnotation.NONE);
		
		this.innerDefinition = definition;
		
		member = new InnerDefinitionMember(definition.getPosition(), outer, definition.getModifiers(), definition.getCompiled());
	}
	
	@Override
	public void registerInnerTypes(Map<String, CompilingType> inner) {
		inner.put(innerDefinition.getCompiled().name, innerDefinition);
	}
	
	@Override
	public void linkTypes(TypeResolutionContext context) {
		if (typesCompiled)
			return;
		typesCompiled = true;
		
		System.out.println("compileTypes " + definition.name + "::" + innerDefinition.getCompiled().name);
		innerDefinition.linkTypes(context);
	}

	@Override
	public InnerDefinitionMember getCompiled() {
		return member;
	}

	@Override
	public void compile(BaseScope scope) {
		innerDefinition.compile(scope);
	}
	
	@Override
	public void registerMembers(BaseScope scope, PrecompilationState state) {
		innerDefinition.registerMembers(scope, state);
	}
}
