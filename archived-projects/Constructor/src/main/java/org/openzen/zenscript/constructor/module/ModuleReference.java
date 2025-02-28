/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.module.logging.*;

/**
 * @author Hoofdgebruiker
 */
public interface ModuleReference {
	String getName();

	SemanticModule load(ModuleLoader loader, ModuleLogger exceptionLogger);

	SourcePackage getRootPackage();
}
