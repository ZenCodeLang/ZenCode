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
public class StatementEncoding {
	private StatementEncoding() {}
	
	public static final int FLAG_POSITION = 1;
	public static final int FLAG_ANNOTATIONS = 2;
	public static final int FLAG_FINAL = 4;
	public static final int FLAG_NAME = 8;
	public static final int FLAG_LABEL = 16;
	
	public static final int TYPE_NULL = 0;
	public static final int TYPE_BLOCK = 1;
	public static final int TYPE_BREAK = 2;
	public static final int TYPE_CONTINUE = 3;
	public static final int TYPE_DO_WHILE = 4;
	public static final int TYPE_EMPTY = 5;
	public static final int TYPE_EXPRESSION = 6;
	public static final int TYPE_FOREACH = 7;
	public static final int TYPE_IF = 8;
	public static final int TYPE_LOCK = 9;
	public static final int TYPE_RETURN = 10;
	public static final int TYPE_SWITCH = 11;
	public static final int TYPE_THROW = 12;
	public static final int TYPE_TRY_CATCH = 13;
	public static final int TYPE_VAR = 14;
	public static final int TYPE_WHILE = 15;
}
