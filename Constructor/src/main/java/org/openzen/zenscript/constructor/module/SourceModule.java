/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.util.Map;
import java.util.function.Consumer;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.constructor.ModuleLoader;

/**
 *
 * @author Hoofdgebruiker
 */
public interface SourceModule {
	public String getName();
	
	public SourcePackage getRootPackage();
	
	public SemanticModule[] loadDependencies(ModuleLoader loader, GlobalTypeRegistry registry, Consumer<CompileException> exceptionLogger);
	
	public Map<String, ISymbol> getGlobals(SemanticModule module);
}
