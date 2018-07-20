/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class ParsedFunctionalMember extends ParsedDefinitionMember {
	protected final CodePosition position;
	protected final int modifiers;
	protected final ParsedImplementation implementation;
	protected final ParsedFunctionBody body;
	private boolean precompiled = false;
	
	public ParsedFunctionalMember(
			CodePosition position,
			HighLevelDefinition definition,
			ParsedImplementation implementation,
			int modifiers,
			ParsedAnnotation[] annotations,
			ParsedFunctionBody body) {
		super(definition, annotations);
		
		this.implementation = implementation;
		this.position = position;
		this.modifiers = modifiers;
		this.body = body;
	}
	
	@Override
	public void linkInnerTypes() {
		
	}
	
	@Override
	public abstract FunctionalMember getCompiled();

	@Override
	public boolean inferHeaders(BaseScope scope, PrecompilationState state) {
		if (precompiled)
			return true;
		precompiled = true;
		
		if ((implementation != null && !Modifiers.isPrivate(modifiers))) {
			fillOverride(scope, implementation.getCompiled().type, state);
			getCompiled().modifiers |= Modifiers.PUBLIC;
		} else if (implementation == null && Modifiers.isOverride(modifiers)) {
			if (definition.superType == null)
				throw new CompileException(position, CompileExceptionCode.OVERRIDE_WITHOUT_BASE, "Override specified without base type");
			
			fillOverride(scope, definition.superType, state);
		}
		
		if (getCompiled().header.returnType == BasicTypeID.UNDETERMINED) {
			ITypeID returnType = body.precompileForResultType(new FunctionScope(scope, getCompiled().header), state);
			if (returnType == null)
				return false;
			
			getCompiled().header = getCompiled().header.withReturnType(returnType);
		}
		return true;
	}
	
	@Override
	public void compile(BaseScope scope, PrecompilationState state) {
		inferHeaders(scope, state);
		
		FunctionScope innerScope = new FunctionScope(scope, getCompiled().header);
		getCompiled().annotations = ParsedAnnotation.compileForMember(annotations, getCompiled(), scope);
		getCompiled().setBody(body.compile(innerScope, getCompiled().header));
	}
	
	protected abstract void fillOverride(TypeScope scope, ITypeID baseType, PrecompilationState state);
}
