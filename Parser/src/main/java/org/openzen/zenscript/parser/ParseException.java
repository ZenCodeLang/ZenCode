/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParseException extends RuntimeException {
	public ParseException(ZSToken token, String message) {
		this(token.position, message);
	}
	
	public ParseException(CodePosition position, String message) {
		super(position.toString() + ": " + message);
	}
}
