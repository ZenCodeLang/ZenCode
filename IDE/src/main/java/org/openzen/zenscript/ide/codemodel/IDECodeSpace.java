/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.codemodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.constructor.Library;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.module.DirectoryModuleReference;
import org.openzen.zenscript.constructor.module.ModuleReference;
import org.openzen.zenscript.ide.host.DevelopmentHost;
import org.openzen.zenscript.ide.host.IDEModule;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.view.output.ErrorOutputSpan;
import org.openzen.zenscript.ide.ui.view.output.OutputLine;
import org.openzen.zenscript.validator.ValidationLogEntry;
import stdlib.Strings;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDECodeSpace {
	private final DevelopmentHost host;
	private final Map<IDESourceFile, List<IDECodeError>> sourceFileErrors = new HashMap<>();
	
	public IDECodeSpace(DevelopmentHost host) {
		this.host = host;
		
		ModuleLoader loader = new ModuleLoader(new CompilationUnit(), exception -> {
			
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
