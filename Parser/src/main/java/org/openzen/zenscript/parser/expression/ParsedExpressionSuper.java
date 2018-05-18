/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionSuper extends ParsedExpression {
	public ParsedExpressionSuper(CodePosition position) {
		super(position);
	}
	
	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID type = scope.getThisType();
		if (type.getSuperType() == null)
			throw new CompileException(position, CompileExceptionCode.SUPER_CALL_NO_SUPERCLASS, "Type has no superclass");
		
		return new PartialTypeExpression(position, scope.getThisType().getSuperType(), null);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
