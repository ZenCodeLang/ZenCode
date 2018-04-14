/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.definitions;

import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.FunctionScope;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedFunction extends ParsedDefinition {
	public static ParsedFunction parseFunction(CodePosition position, int modifiers, ZSTokenStream parser, HighLevelDefinition outerDefinition) {
		String name = parser.required(T_IDENTIFIER, "identifier expected").content;
		ParsedFunctionHeader header = ParsedFunctionHeader.parse(parser);
		ParsedFunctionBody body = ParsedStatement.parseFunctionBody(parser);
		return new ParsedFunction(position, modifiers, name, header, body, outerDefinition);
	}
	
	private final String name;
	private final ParsedFunctionHeader header;
	private final ParsedFunctionBody body;

	private final FunctionDefinition compiled;
	
	private ParsedFunction(CodePosition position, int modifiers, String name, ParsedFunctionHeader header, ParsedFunctionBody body, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.name = name;
		this.header = header;
		this.body = body;
		
		compiled = new FunctionDefinition(name, modifiers, outerDefinition);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void linkInnerTypes() {
		// nothing to do
	}

	@Override
	public void compileMembers(BaseScope scope) {
		compiled.setHeader(header.compile(scope));
	}

	@Override
	public void compileCode(BaseScope scope) {
		FunctionScope innerScope = new FunctionScope(scope, compiled.header);
		compiled.setCode(body.compile(innerScope, compiled.header));
	}
}
