/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.util.function.Consumer;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.constructor.ModuleLoader;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ModuleReference {
	public String getName();
	
	public SemanticModule load(ModuleLoader loader, CompilationUnit unit, Consumer<CompileException> exceptionLogger);
	
	public SourcePackage getRootPackage();
}
