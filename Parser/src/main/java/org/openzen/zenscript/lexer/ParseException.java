/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import org.openzen.zencode.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParseException extends Exception {
	public final CodePosition position;
	public final String message;
	
	public ParseException(CodePosition position, String message) {
		super(position.toString() + ": " + message);
		
		this.position = position;
		this.message = message;
	}
	
	public ParseException(CodePosition position, String message, Throwable cause) {
		super(position + ": " + message, cause);
		
		this.position = position;
		this.message = message;
	}
}
