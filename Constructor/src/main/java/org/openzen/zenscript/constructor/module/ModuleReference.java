/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.CompilationUnit;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ModuleReference {
	public String getName();
	
	public SemanticModule load(CompilationUnit unit);
	
	public SourcePackage getRootPackage();
}
