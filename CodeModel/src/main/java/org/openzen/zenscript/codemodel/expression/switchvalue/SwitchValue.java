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
public interface SwitchValue {
	<T> T accept(SwitchValueVisitor<T> visitor);
	
	<C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor);
}
