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
public class FunctionHeaderEncoding {
	private FunctionHeaderEncoding() {}
	
	public static final int FLAG_TYPE_PARAMETERS = 1;
	public static final int FLAG_RETURN_TYPE = 2;
	public static final int FLAG_THROWS = 4;
	public static final int FLAG_PARAMETERS = 8;
	public static final int FLAG_VARIADIC = 16;
	public static final int FLAG_DEFAULT_VALUES = 32;
}
