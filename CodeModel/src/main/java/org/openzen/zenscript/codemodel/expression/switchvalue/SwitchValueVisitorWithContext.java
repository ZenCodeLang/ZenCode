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
	R acceptInt(C context, IntSwitchValue value);
	
	R acceptChar(C context, CharSwitchValue value);
	
	R acceptString(C context, StringSwitchValue value);
	
	R acceptEnumConstant(C context, EnumConstantSwitchValue value);
	
	R acceptVariantOption(C context, VariantOptionSwitchValue value);
}
