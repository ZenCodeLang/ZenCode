/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class VariantValueExpression extends Expression {
	public final VariantDefinition.Option option;
	public final Expression[] arguments;
	
	public VariantValueExpression(CodePosition position, ITypeID variantType, VariantDefinition.Option option) {
		this(position, variantType, option, null);
	}
	
	public VariantValueExpression(CodePosition position, ITypeID variantType, VariantDefinition.Option option, Expression[] arguments) {
		super(position, variantType);
		
		this.option = option;
		this.arguments = null;
	}
	
	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		return new VariantValueExpression(position, type, option, arguments.arguments);
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
