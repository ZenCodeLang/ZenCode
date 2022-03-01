package org.openzen.zenscript.rustsource.compiler;

import org.openzen.zenscript.rustsource.definitions.RustModule;

import java.util.*;
import java.util.stream.Collectors;

public class ImportSet {
	private final Map<String, Set<String>> imports = new HashMap<>();
	private final Map<String, String> simpleImports = new HashMap<>();

	public String addImport(RustModule module, String name) {
		String path = getPath(module);
		if (simpleImports.containsKey(name)) {
			if (simpleImports.get(name).equals(path)) {
				return name;
			} else {
				return getPath(module) + name;
			}
		}

		imports.computeIfAbsent(path, k -> new HashSet<>()).add(name);
		return name;
	}

	public void compileTo(StringBuilder builder) {
		List<String> importModules = imports.keySet().stream().sorted().collect(Collectors.toList());
		for (String path : importModules) {
			builder.append("use ");
			builder.append(path);
			Set<String> items = imports.get(path);
			if (items.size() == 1) {
				for (String item : items)
					builder.append(item);
			} else {
				builder.append("{");
				boolean first = true;
				for (String item : items) {
					if (first) {
						first = false;
					} else {
						builder.append(", ");
					}
					builder.append(item);
				}
				builder.append("}");
			}
			builder.append(";\n");
		}
	}

	private static String getPath(RustModule module) {
		StringBuilder result = new StringBuilder();
		if (module.crate) {
			result.append("crate::");
		}
		for (String path : module.path) {
			result.append(path);
			result.append("::");
		}
		return result.toString();
	}
}
