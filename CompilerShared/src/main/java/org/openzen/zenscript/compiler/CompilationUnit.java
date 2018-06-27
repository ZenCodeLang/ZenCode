/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

/**
 *
 * @author Hoofdgebruiker
 */
public class CompilationUnit {
	public final ZSPackage stdlib = new ZSPackage(null, "stdlib");
	public final GlobalTypeRegistry globalTypeRegistry = new GlobalTypeRegistry(stdlib);
	
	public CompilationUnit() {
		
	}
}
