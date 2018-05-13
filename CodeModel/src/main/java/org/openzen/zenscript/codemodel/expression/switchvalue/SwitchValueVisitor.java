/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression.switchvalue;

/**
 *
 * @author Hoofdgebruiker
 */
public interface SwitchValueVisitor <T> {
	public T acceptInt(IntSwitchValue value);
	
	public T acceptChar(CharSwitchValue value);
	
	public T acceptString(StringSwitchValue value);
	
	public T acceptEnumConstant(EnumConstantSwitchValue value);
	
	public T acceptVariantOption(VariantOptionSwitchValue value);
}
