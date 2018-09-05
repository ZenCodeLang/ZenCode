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
public class DefinitionEncoding {
	private DefinitionEncoding() {}
	
	public static final int FLAG_POSITION = 1;
	public static final int FLAG_NAME = 2;
	public static final int FLAG_TYPE_PARAMETERS = 4;
	
	public static final int TYPE_CLASS = 1;
	public static final int TYPE_STRUCT = 2;
	public static final int TYPE_INTERFACE = 3;
	public static final int TYPE_ENUM = 4;
	public static final int TYPE_VARIANT = 5;
	public static final int TYPE_FUNCTION = 6;
	public static final int TYPE_ALIAS = 7;
	public static final int TYPE_EXPANSION = 8;
}
