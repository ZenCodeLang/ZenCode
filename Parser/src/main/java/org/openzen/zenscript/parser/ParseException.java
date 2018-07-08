/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParseException extends RuntimeException {
	public ParseException(CodePosition position, String message) {
		super(position.toString() + ": " + message);
	}
}
