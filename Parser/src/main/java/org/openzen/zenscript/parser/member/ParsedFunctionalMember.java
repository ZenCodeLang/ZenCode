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
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.parser.ParsedAnnotation;
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
	private boolean isCompiled = false;
	
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
	public abstract FunctionalMember getCompiled();
	
	protected void inferHeaders(BaseScope scope) throws CompileException {
		if ((implementation != null && !Modifiers.isPrivate(modifiers))) {
			fillOverride(scope, implementation.getCompiled().type.stored(BorrowStorageTag.THIS));
		} else if (implementation == null && Modifiers.isOverride(modifiers)) {
			if (definition.getSuperType() == null)
				throw new CompileException(position, CompileExceptionCode.OVERRIDE_WITHOUT_BASE, "Override specified without base type");
			
			fillOverride(scope, definition.getSuperType().stored(BorrowStorageTag.THIS));
		}
		
		if (getCompiled() == null || getCompiled().header == null)
			throw new IllegalStateException("Types not yet linked");
	}
	
	@Override
	public final void compile(BaseScope scope) throws CompileException {
		if (isCompiled)
			return;
		isCompiled = true;
		
		inferHeaders(scope);
		
		FunctionScope innerScope = new FunctionScope(position, scope, getCompiled().header);
		getCompiled().annotations = ParsedAnnotation.compileForMember(annotations, getCompiled(), scope);
		getCompiled().setBody(body.compile(innerScope, getCompiled().header));
		
		if (getCompiled().header.getReturnType().isBasic(BasicTypeID.UNDETERMINED)) {
			StoredType returnType = getCompiled().body.getReturnType();
			if (returnType == null) {
				throw new CompileException(position, CompileExceptionCode.CANNOT_INFER_RETURN_TYPE, "Method return type could not be inferred");
			} else {
				getCompiled().header.setReturnType(returnType);
			}
		}
	}
	
	protected abstract void fillOverride(TypeScope scope, StoredType baseType) throws CompileException;
}
