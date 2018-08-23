/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

/**
 * @author Hoofdgebruiker
 */
public class JavaParameterInfo {
	public final int index;
	public final String typeDescriptor;

	public JavaParameterInfo(int index, String typeDescriptor) {
		this.index = index;
		this.typeDescriptor = typeDescriptor;
	}
}
