/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.statements.ParsedStatement;

/**
 *
 * @author Stanneke
 */
public class ParsedFunction extends ParsedDefinition {
	public static ParsedFunction parseFunction(ZSPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser parser, HighLevelDefinition outerDefinition) {
		String name = parser.required(T_IDENTIFIER, "identifier expected").content;
		ParsedFunctionHeader header = ParsedFunctionHeader.parse(parser);
		ParsedFunctionBody body = ParsedStatement.parseFunctionBody(parser);
		return new ParsedFunction(pkg, position, modifiers, annotations, name, header, body, outerDefinition);
	}
	
	private final ParsedFunctionHeader header;
	private final ParsedFunctionBody body;

	private final FunctionDefinition compiled;
	
	private ParsedFunction(ZSPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, ParsedFunctionHeader header, ParsedFunctionBody body, HighLevelDefinition outerDefinition) {
		super(position, modifiers, annotations);
		
		this.header = header;
		this.body = body;
		
		compiled = new FunctionDefinition(position, pkg, name, modifiers, outerDefinition);
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
	public void listMembers(BaseScope scope, PrecompilationState state) {
		
	}

	@Override
	public void compileCode(BaseScope scope, PrecompilationState state) {
		FunctionScope innerScope = new FunctionScope(scope, compiled.header);
		compiled.setCode(body.compile(innerScope, compiled.header));
		
		if (compiled.header.returnType == BasicTypeID.UNDETERMINED) {
			ITypeID result = body.precompileForResultType(new FunctionScope(scope, compiled.header), state);
			if (result == null)
				throw new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "Could not determine return type for method " + compiled.name);
			compiled.header = compiled.header.withReturnType(result);
		}
	}
}
