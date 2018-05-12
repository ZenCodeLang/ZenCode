/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class EnumConstantExpression extends Expression {
	public final EnumConstantMember value;
	
	public EnumConstantExpression(CodePosition position, ITypeID type, EnumConstantMember value) {
		super(position, type);
		
		this.value = value;
	}
	
	public EnumConstantExpression(CodePosition position, GlobalTypeRegistry registry, EnumDefinition type, EnumConstantMember value) {
		super(position, registry.getForDefinition(type, null));
		
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitEnumConstant(this);
	}
}
