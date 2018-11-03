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
	T acceptInt(IntSwitchValue value);
	
	T acceptChar(CharSwitchValue value);
	
	T acceptString(StringSwitchValue value);
	
	T acceptEnumConstant(EnumConstantSwitchValue value);
	
	T acceptVariantOption(VariantOptionSwitchValue value);
}
