/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host;

import java.util.function.Consumer;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.ide.codemodel.IDECodeError;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDEModule {
	public String getName();
	
	public IDEPackage getRootPackage();
	
	void prebuild(ModuleLoader loader, Consumer<IDECodeError> errors);
}
