/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ModuleReference {
	String getModuleName();
	
	SemanticModule load(ModuleRegistry modules);
}
