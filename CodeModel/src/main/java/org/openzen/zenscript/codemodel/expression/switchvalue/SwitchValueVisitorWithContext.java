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
public interface SwitchValueVisitorWithContext <C, R> {
	public R acceptInt(C context, IntSwitchValue value);
	
	public R acceptChar(C context, CharSwitchValue value);
	
	public R acceptString(C context, StringSwitchValue value);
	
	public R acceptEnumConstant(C context, EnumConstantSwitchValue value);
	
	public R acceptVariantOption(C context, VariantOptionSwitchValue value);
}
