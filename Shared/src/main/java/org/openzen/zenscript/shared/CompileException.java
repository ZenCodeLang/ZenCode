/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.shared;

/**
 *
 * @author Hoofdgebruiker
 */
public class CompileException extends RuntimeException {
	public final CodePosition position;
	public final CompileExceptionCode code;
	
	public CompileException(CodePosition position, CompileExceptionCode code, String message) {
		super(position.toString() + ": [" + code + "]" + message);
		
		this.position = position;
		this.code = code;
	}
}
