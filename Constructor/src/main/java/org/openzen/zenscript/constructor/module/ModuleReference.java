/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ModuleReference {
	public String getName();
	
	public SemanticModule load();
	
	public SourcePackage getRootPackage();
}
