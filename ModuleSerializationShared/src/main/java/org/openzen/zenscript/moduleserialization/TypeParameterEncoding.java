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
public class TypeParameterEncoding {
	private TypeParameterEncoding() {}
	
	public static final int FLAG_POSITION = 1;
	public static final int FLAG_NAME = 2;
	
	public static final int TYPE_TYPE_BOUND = 1;
	public static final int TYPE_SUPER_BOUND = 2;
}
