/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserialization;

/**
 * @author Hoofdgebruiker
 */
public class CodePositionEncoding {
	public static final int FLAG_FILE = 1;
	public static final int FLAG_FROM_LINE = 2;
	public static final int FLAG_FROM_OFFSET = 4;
	public static final int FLAG_TO_LINE = 8;
	public static final int FLAG_TO_OFFSET = 16;

	private CodePositionEncoding() {
	}
}
