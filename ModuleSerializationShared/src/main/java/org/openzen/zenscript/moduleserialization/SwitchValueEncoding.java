/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserialization;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwitchValueEncoding {
	private SwitchValueEncoding() {}
	
	public static final int TYPE_NULL = 0;
	public static final int TYPE_INT = 1;
	public static final int TYPE_CHAR = 2;
	public static final int TYPE_STRING = 3;
	public static final int TYPE_ENUM = 4;
	public static final int TYPE_VARIANT_OPTION = 5;
}
