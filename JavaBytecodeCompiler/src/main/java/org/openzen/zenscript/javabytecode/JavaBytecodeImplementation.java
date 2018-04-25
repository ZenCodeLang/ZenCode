/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.openzen.zenscript.javabytecode.compiler.JavaWriter;

/**
 *
 * @author Hoofdgebruiker
 */
public interface JavaBytecodeImplementation {
	void compile(JavaWriter writer);
}
