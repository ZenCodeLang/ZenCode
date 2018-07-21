/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Type;

/**
 * @author Hoofdgebruiker
 */
public class JavaParameterInfo {
	public final int index;
	public final Type type;

	public JavaParameterInfo(int index, Type type) {
		this.index = index;
		this.type = type;
	}
}
