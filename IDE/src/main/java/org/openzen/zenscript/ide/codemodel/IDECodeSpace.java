/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.codemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDESourceFile;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDECodeSpace {
	private final DevelopmentHost host;
	private final Map<IDESourceFile, List<IDECodeError>> sourceFileErrors = new HashMap<>();
	
	public IDECodeSpace(DevelopmentHost host) {
		this.host = host;
		
		ZSPackage root = ZSPackage.createRoot();
		ZSPackage stdlib = new ZSPackage(root, "stdlib");
		GlobalTypeRegistry registry = new GlobalTypeRegistry(stdlib);
		ModuleLoader loader = new ModuleLoader(registry, exception -> {
			exception.printStackTrace();
		});
		for (IDEModule module : host.getModules()) {
			module.prebuild(loader, this::addError);
		}
	}
	
	public void onSaved(IDESourceFile file) {
		
	}
	
	public List<IDECodeError> getErrors(IDESourceFile file) {
		return sourceFileErrors.getOrDefault(file, Collections.emptyList());
	}
	
	private void addError(IDECodeError error) {
		if (error.file == null)
			return;
		if (!sourceFileErrors.containsKey(error.file))
			sourceFileErrors.put(error.file, new ArrayList<>());
		sourceFileErrors.get(error.file).add(error);
	}
}
