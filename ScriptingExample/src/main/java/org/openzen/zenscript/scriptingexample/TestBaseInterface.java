/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import org.openzen.zencode.java.ZenCodeType;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TestBaseInterface<T> extends ZenCodeType {
	@Method
	T getValue();
}
