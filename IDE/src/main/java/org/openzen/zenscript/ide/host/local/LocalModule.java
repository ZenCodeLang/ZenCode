/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEPackage;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalModule implements IDEModule {
	private final ModuleReference module;
	
	public LocalModule(ModuleReference module) {
		this.module = module;
	}

	@Override
	public String getName() {
		return module.getName();
	}

	@Override
	public IDEPackage getRootPackage() {
		return new LocalPackage(module.getRootPackage());
	}
}
