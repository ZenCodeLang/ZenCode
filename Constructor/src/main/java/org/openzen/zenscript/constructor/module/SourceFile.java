/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.io.File;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourceFile {
	public final String name;
	public final File file;
	
	public SourceFile(String name, File file) {
		this.name = name;
		this.file = file;
	}
}
