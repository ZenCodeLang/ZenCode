/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface CompilerOutput {
	void info(String message);
	
	void warning(String message);
	
	void warning(CodePosition position, String message);
	
	void error(String message);
	
	void error(CodePosition position, String message);
}
