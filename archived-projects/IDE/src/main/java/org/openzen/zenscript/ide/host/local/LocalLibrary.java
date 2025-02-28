/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import live.LiveArrayList;
import live.LiveList;
import live.MutableLiveList;

import org.openzen.zenscript.constructor.Library;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.ide.host.IDELibrary;
import org.openzen.zenscript.ide.host.IDEModule;

/**
 * @author Hoofdgebruiker
 */
public class LocalLibrary implements IDELibrary {
	private final Library library;
	private final MutableLiveList<IDEModule> modules;

	public LocalLibrary(Library library) {
		this.library = library;
		modules = new LiveArrayList<>();
		for (ModuleReference module : library.modules)
			modules.add(new LocalModule(module));
	}

	@Override
	public String getName() {
		return library.name;
	}

	@Override
	public LiveList<IDEModule> getModules() {
		return modules;
	}
}
