/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import java.util.function.Consumer;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.ide.codemodel.IDECodeError;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDEModuleType;
import org.openzen.zenscript.ide.host.IDEPackage;
import org.openzen.zenscript.validator.Validator;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalModule implements IDEModule {
	private final ModuleReference module;
	private final LocalPackage rootPackage;
	
	public LocalModule(ModuleReference module) {
		this.module = module;
		rootPackage = new LocalPackage(module.getRootPackage());
	}

	@Override
	public String getName() {
		return module.getName();
	}
	
	@Override
	public IDEModuleType getType() {
		return UniversalModuleType.INSTANCE;
	}

	@Override
	public IDEPackage getRootPackage() {
		return rootPackage;
	}
	
	@Override
	public void prebuild(ModuleLoader loader, Consumer<IDECodeError> errors) {
		SemanticModule module = loader.getModule(this.module.getName());
		Validator.validate(module, entry -> errors.accept(new IDECodeError(null, entry.position, entry.message)));
	}
}
