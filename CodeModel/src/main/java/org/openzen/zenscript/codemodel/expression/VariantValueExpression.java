/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;

/**
 *
 * @author Hoofdgebruiker
 */
public class VariantValueExpression extends Expression {
	public final VariantOptionRef option;
	public final Expression[] arguments;
	
	public VariantValueExpression(CodePosition position, StoredType variantType, VariantOptionRef option) {
		this(position, variantType, option, Expression.NONE);
	}
	
	public VariantValueExpression(CodePosition position, StoredType variantType, VariantOptionRef option, Expression[] arguments) {
		super(position, variantType, multiThrow(position, arguments));
		
		this.option = option;
		this.arguments = arguments;
	}
	
	public int getNumberOfArguments() {
		return arguments == null ? 0 : arguments.length;
	}
	
	@Override
	public Expression call(CodePosition position, TypeScope scope, List<StoredType> hints, CallArguments arguments) throws CompileException {
		if (arguments != null)
			return super.call(position, scope, hints, arguments);
		
		return new VariantValueExpression(position, type, option, arguments.arguments);
	}
	
	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitVariantValue(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitVariantValue(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression[] tArguments = Expression.transform(arguments, transformer);
		return tArguments == arguments ? this : new VariantValueExpression(position, type, option, tArguments);
	}

	@Override
	public Expression normalize(TypeScope scope) {
		Expression[] normalized = new Expression[arguments.length];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = arguments[i].normalize(scope).castImplicit(position, scope, option.types[i]);
		return new VariantValueExpression(position, type, option, normalized);
	}
}
