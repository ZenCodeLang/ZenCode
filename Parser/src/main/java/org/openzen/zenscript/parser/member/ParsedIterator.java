/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.CustomIteratorMember;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedIterator extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedFunctionHeader header;
	private final ParsedFunctionBody body;
	
	private CustomIteratorMember compiled;
	
	public ParsedIterator(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			ParsedAnnotation[] annotations,
			ParsedFunctionHeader header,
			ParsedFunctionBody body)
	{
		super(definition, annotations);
		
		this.position = position;
		this.modifiers = modifiers;
		this.header = header;
		this.body = body;
	}

	@Override
	public void linkInnerTypes() {
		// nothing to do
	}

	@Override
	public void linkTypes(BaseScope scope) {
		ITypeID[] loopVariableTypes = new ITypeID[header.parameters.size()];
		for (int i = 0; i < loopVariableTypes.length; i++)
			loopVariableTypes[i] = header.parameters.get(i).type.compile(scope);
		
		compiled = new CustomIteratorMember(position, definition, modifiers, loopVariableTypes);
	}

	@Override
	public CustomIteratorMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(BaseScope scope) {
		FunctionHeader header = new FunctionHeader(scope.getTypeRegistry().getIterator(compiled.getLoopVariableTypes()));
		StatementScope innerScope = new FunctionScope(scope, header);
		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, scope);
		compiled.setContent(body.compile(innerScope, header));
	}
}
