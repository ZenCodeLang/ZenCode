/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.util.Map;

import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.constructor.ModuleLoader;

/**
 * @author Hoofdgebruiker
 */
public interface SourceModule {
	String getName();

	SourcePackage getRootPackage();

	SemanticModule[] loadDependencies(ModuleLoader loader, CompileExceptionLogger exceptionLogger);

	Map<String, IGlobal> getGlobals(SemanticModule module);
}
