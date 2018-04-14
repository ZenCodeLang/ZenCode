/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ICasterMember extends IDefinitionMember {
	public ITypeID getTargetType();
	
	public Expression cast(CodePosition position, Expression value, ITypeID toType);
	
	public boolean isImplicit();
}
