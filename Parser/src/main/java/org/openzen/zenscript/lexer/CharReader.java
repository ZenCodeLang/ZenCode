/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.io.IOException;

/**
 *
 * @author Hoofdgebruiker
 */
public interface CharReader {
	int peek() throws IOException;
	
	int next() throws IOException;
}
