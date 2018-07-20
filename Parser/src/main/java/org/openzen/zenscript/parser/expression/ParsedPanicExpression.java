/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.PanicExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedPanicExpression extends ParsedExpression {
	public final ParsedExpression value;
	
	public ParsedPanicExpression(CodePosition position, ParsedExpression value) {
		super(position);
		
		this.value = value;
	}
	
	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new PanicExpression(position, scope.getResultTypeHints().isEmpty() ? BasicTypeID.VOID : scope.getResultTypeHints().get(0), value.compile(scope).eval());
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}

	@Override
	public ITypeID precompileForType(ExpressionScope scope, PrecompilationState state) {
		return null;
	}
}
