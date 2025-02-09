/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host;

import live.LiveList;

/**
 * @author Hoofdgebruiker
 */
public interface DevelopmentHost {
	public String getName();

	public IDEPropertyStore getPropertyStore();

	public LiveList<IDEModule> getModules();

	public LiveList<IDELibrary> getLibraries();

	public LiveList<IDETarget> getTargets();
}
