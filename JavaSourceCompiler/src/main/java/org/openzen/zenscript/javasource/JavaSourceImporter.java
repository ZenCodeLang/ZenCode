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
import org.openzen.zenscript.javasource.tags.JavaSourceClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceImporter {
	private final JavaSourceClass cls;
	private final Map<String, JavaSourceClass> imports = new HashMap<>();
	private final Set<JavaSourceClass> usedImports = new HashSet<>();
	
	public JavaSourceImporter(JavaSourceClass cls) {
		this.cls = cls;
	}
	
	public String importType(HighLevelDefinition definition) {
		JavaSourceClass cls = definition.getTag(JavaSourceClass.class);
		if (cls == null)
			throw new IllegalStateException("Missing source class tag on " + definition.name);
		if (cls.pkg.equals(this.cls.pkg))
			return cls.getClassName();
		
		return importType(cls);
	}
	
	public String importType(JavaSourceClass cls) {
		if (imports.containsKey(cls.outer.getName())) {
			JavaSourceClass imported = imports.get(cls.outer.getName());
			usedImports.add(imported);
			return imported.fullName.equals(cls.outer.fullName) ? cls.getName() : cls.fullName;
		}
		
		imports.put(cls.outer.getName(), cls.outer);
		usedImports.add(cls.outer);
		return cls.getClassName();
	}
	
	public JavaSourceClass[] getUsedImports() {
		JavaSourceClass[] imports = usedImports.toArray(new JavaSourceClass[usedImports.size()]);
		Arrays.sort(imports);
		return imports;
	}
}
