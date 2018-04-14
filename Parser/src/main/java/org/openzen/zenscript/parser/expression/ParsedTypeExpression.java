/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeExpression extends ParsedExpression {
	private final IParsedType type;
	
	public ParsedTypeExpression(CodePosition position, IParsedType type) {
		super(position);
		
		this.type = type;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new PartialTypeExpression(position, type.compile(scope));
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
