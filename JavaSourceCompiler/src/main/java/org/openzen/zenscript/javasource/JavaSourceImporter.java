/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceImporter {
	private final ZSPackage pkg;
	private final Map<String, Import> imports = new HashMap<>();
	private final Set<Import> usedImports = new HashSet<>();
	
	public JavaSourceImporter(ZSPackage pkg) {
		this.pkg = pkg;
		
		imports.put("IllegalArgumentException", new Import("IllegalArgumentException", "stdlib.IllegalArgumentException", "java.lang.IllegalArgumentException"));
		imports.put("StringBuilder", new Import("StringBuilder", "stdlib.StringBuilder", "java.lang.StringBuilder"));
	}
	
	public String importType(HighLevelDefinition definition) {
		if (definition.pkg == pkg)
			return definition.name;
		
		return importType(definition.pkg.fullName + "." + definition.name);
	}
	
	public String importType(String fullName) {
		String name = fullName.substring(fullName.lastIndexOf('.') + 1);
		if (imports.containsKey(name)) {
			Import imported = imports.get(name);
			usedImports.add(imported);
			return imported.originalName.equals(fullName) ? name : fullName;
		}
		
		Import imported = new Import(name, fullName, fullName);
		imports.put(name, imported);
		return name;
	}
	
	public Import[] getUsedImports() {
		Import[] imports = usedImports.toArray(new Import[usedImports.size()]);
		Arrays.sort(imports);
		return imports;
	}
	
	public static class Import implements Comparable<Import> {
		public final String name;
		public final String originalName;
		public final String actualName;
		
		public Import(String name, String originalName, String actualName) {
			this.name = name;
			this.originalName = originalName;
			this.actualName = actualName;
		}

		@Override
		public int compareTo(Import o) {
			return actualName.compareTo(o.actualName);
		}
	}
}
