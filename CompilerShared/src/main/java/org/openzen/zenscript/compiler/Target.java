/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.SemanticModule;

/**
 *
 * @author Hoofdgebruiker
 */
public interface Target {
	public ZenCodeCompiler createCompiler(SemanticModule module, IZSLogger logger);
	
	public String getModule();
	
	public String getName();
	
	public boolean canRun();
	
	public boolean canBuild();
}
