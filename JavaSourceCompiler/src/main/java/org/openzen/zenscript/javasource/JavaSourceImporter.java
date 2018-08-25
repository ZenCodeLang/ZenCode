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
import org.openzen.zenscript.javashared.JavaClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceImporter {
	private final JavaClass cls;
	private final Map<String, JavaClass> imports = new HashMap<>();
	private final Set<JavaClass> usedImports = new HashSet<>();
	
	public JavaSourceImporter(JavaClass cls) {
		this.cls = cls;
	}
	
	public String importType(HighLevelDefinition definition) {
		JavaClass cls = definition.getTag(JavaClass.class);
		if (cls == null)
			throw new IllegalStateException("Missing source class tag on " + definition.name);
		
		return importType(cls);
	}
	
	public String importType(JavaClass cls) {
		if (imports.containsKey(cls.outer.getName())) {
			JavaClass imported = imports.get(cls.outer.getName());
			usedImports.add(imported);
			return imported.fullName.equals(cls.outer.fullName) ? cls.getName() : cls.fullName;
		}
		if (cls.pkg.equals(this.cls.pkg))
			return cls.getClassName();
		
		imports.put(cls.outer.getName(), cls.outer);
		usedImports.add(cls.outer);
		return cls.getClassName();
	}
	
	public JavaClass[] getUsedImports() {
		JavaClass[] imports = usedImports.toArray(new JavaClass[usedImports.size()]);
		Arrays.sort(imports);
		return imports;
	}
}
