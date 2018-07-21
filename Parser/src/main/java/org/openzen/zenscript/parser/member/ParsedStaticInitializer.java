/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.statements.ParsedStatement;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStaticInitializer extends ParsedDefinitionMember {
	private final ParsedStatement body;
	
	private final StaticInitializerMember compiled;
	
	public ParsedStaticInitializer(HighLevelDefinition definition, CodePosition position, ParsedAnnotation[] annotations, ParsedStatement body) {
		super(definition, annotations);
		
		this.body = body;
		compiled = new StaticInitializerMember(position);
	}

	@Override
	public void linkInnerTypes() {
		
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, scope);
	}

	@Override
	public IDefinitionMember getCompiled() {
		return compiled;
	}

	@Override
	public boolean inferHeaders(BaseScope scope, PrecompilationState state) {
		return true;
	}

	@Override
	public void compile(BaseScope scope, PrecompilationState state) {
		compiled.body = body.compile(new FunctionScope(scope, new FunctionHeader(BasicTypeID.VOID)));
	}
}
