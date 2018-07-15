/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.codemodel;

import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDESourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDECodeSpace {
	private final DevelopmentHost host;
	
	public IDECodeSpace(DevelopmentHost host) {
		this.host = host;
	}
	
	public void onSaved(IDESourceFile file) {
		
	}
}
