/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host;

import live.LiveString;

import org.openzen.zencode.shared.SourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDESourceFile {
	public LiveString getName();
	
	public SourceFile getFile();
	
	public void update(String content);
}
