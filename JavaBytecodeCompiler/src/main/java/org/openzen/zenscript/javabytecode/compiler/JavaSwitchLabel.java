/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSwitchLabel {
	public final int key;
	public final Label label;
	
	public JavaSwitchLabel(int key, Label label) {
		this.key = key;
		this.label = label;
	}
}
