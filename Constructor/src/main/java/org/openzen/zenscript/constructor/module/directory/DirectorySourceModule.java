package org.openzen.zenscript.constructor.module.directory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.globals.TypeGlobal;
import org.openzen.zenscript.constructor.ConstructorException;
import org.openzen.zenscript.constructor.JSONUtils;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.module.SourceModule;
import org.openzen.zenscript.constructor.module.SourcePackage;

public class DirectorySourceModule implements SourceModule {
	private final String moduleName;
	private final boolean isStdLib;
	private final JSONObject json;

	private final SourcePackage rootPackage;

	public DirectorySourceModule(String moduleName, File directory) {
		this(moduleName, directory, false);
	}

	public DirectorySourceModule(String moduleName, File directory, boolean isStdLib) {
		this.moduleName = moduleName;
		this.isStdLib = isStdLib;

		this.rootPackage = new SourceDirectoryPackage(new File(directory, "src"), moduleName);

		if (!directory.exists())
			throw new ConstructorException("Error: module directory not found: " + directory);

		File jsonFile = new File(directory, "module.json");
		if (!jsonFile.exists())
			throw new ConstructorException("Error: module.json file not found in module " + moduleName);

		try {
			json = JSONUtils.load(jsonFile);
		} catch (IOException ex) {
			throw new ConstructorException("Error: could not load module.json: " + ex.getMessage());
		}
	}

	@Override
	public String getName() {
		return moduleName;
	}

	@Override
	public SourcePackage getRootPackage() {
		return rootPackage;
	}

	@Override
	public SemanticModule[] loadDependencies(ModuleLoader loader, CompileExceptionLogger exceptionLogger) {
		List<String> dependencyNames = new ArrayList<>();
		if (!isStdLib)
			dependencyNames.add("stdlib");

		JSONArray jsonDependencies = json.optJSONArray("dependencies");
		if (jsonDependencies != null) {
			for (int i = 0; i < jsonDependencies.length(); i++) {
				dependencyNames.add(jsonDependencies.getString(i));
			}
		}
		// TODO: annotation type registration
		ModuleSpace space = new ModuleSpace(new ArrayList<>());
		SemanticModule[] dependencies = new SemanticModule[dependencyNames.size()];
		for (int i = 0; i < dependencies.length; i++) {
			String dependencyName = dependencyNames.get(i);
			SemanticModule module = loader.getModule(dependencyName);
			dependencies[i] = module;

			try {
				space.addModule(dependencyName, module);
			} catch (CompileException ex) {
				throw new ConstructorException("Error: exception during compilation", ex);
			}
		}

		return dependencies;
	}

	@Override
	public Map<String, IGlobal> getGlobals(SemanticModule module) {
		JSONObject jsonGlobals = json.optJSONObject("globals");
		if (jsonGlobals == null)
			return Collections.emptyMap();

		Map<String, IGlobal> result = new HashMap<>();
		for (String key : jsonGlobals.keySet()) {
			JSONObject global = jsonGlobals.getJSONObject(key);
			if ("Definition".equals(global.getString("type"))) {
				HighLevelDefinition definition = module.definitions.getDefinition(global.getString("definition"));
				if (definition == null)
					throw new ConstructorException("No such definition: " + global.getString("definition"));
				result.put(key, new TypeGlobal(definition));
			} else {
				throw new ConstructorException("Invalid global type: " + global.getString("type"));
			}
		}
		return result;
	}
}
