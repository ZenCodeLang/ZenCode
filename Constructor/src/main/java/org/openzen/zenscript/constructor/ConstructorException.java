/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

/**
 * @author Hoofdgebruiker
 */
public class ConstructorException extends RuntimeException {
	public ConstructorException(String message) {
		super(message);
	}

	public ConstructorException(String message, Throwable cause) {
		super(message, cause);
	}
}
