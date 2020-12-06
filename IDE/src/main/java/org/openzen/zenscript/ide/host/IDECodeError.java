/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host;

import org.openzen.zencode.shared.CodePosition;

/**
 * @author Hoofdgebruiker
 */
public class IDECodeError {
	public final IDESourceFile file;
	public final CodePosition position;
	public final String message;

	public IDECodeError(IDESourceFile file, CodePosition position, String message) {
		this.file = file;
		this.position = position;
		this.message = message;
	}
}
