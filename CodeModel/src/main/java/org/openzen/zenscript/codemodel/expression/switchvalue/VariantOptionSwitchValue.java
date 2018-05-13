/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zenscript.codemodel.definition.VariantDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class VariantOptionSwitchValue implements SwitchValue {
	public final VariantDefinition.Option option;
	public final String[] parameters;
	
	public VariantOptionSwitchValue(VariantDefinition.Option option, String[] parameters) {
		this.option = option;
		this.parameters = parameters;
	}
	
	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptVariantOption(this);
	}
}
