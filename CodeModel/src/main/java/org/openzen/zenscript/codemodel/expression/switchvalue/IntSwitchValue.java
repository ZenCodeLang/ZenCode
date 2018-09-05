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
public class IntSwitchValue implements SwitchValue {
	public final int value;
	
	public IntSwitchValue(int value) {
		this.value = value;
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptInt(this);
	}
	
	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptInt(context, this);
	}
}
