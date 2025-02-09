/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.util.Collection;

import org.openzen.zencode.shared.SourceFile;

/**
 * @author Hoofdgebruiker
 */
public interface SourcePackage {
	public String getName();

	public Collection<? extends SourcePackage> getSubPackages();

	public Collection<? extends SourceFile> getFiles();

	public SourcePackage createSubPackage(String name);

	public SourceFile createSourceFile(String name);
}
