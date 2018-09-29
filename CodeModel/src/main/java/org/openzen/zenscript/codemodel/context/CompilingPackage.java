/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class CompilingPackage {
	public final Module module;
	private final ZSPackage pkg;
	private final Map<String, CompilingPackage> packages = new HashMap<>();
	private final Map<String, CompilingType> types = new HashMap<>();
	
	public CompilingPackage(ZSPackage pkg, Module module) {
		this.pkg = pkg;
		this.module = module;
	}
	
	public ZSPackage getPackage() {
		return pkg;
	}
	
	public CompilingPackage getOrCreatePackage(String name) {
		if (packages.containsKey(name))
			return packages.get(name);
		
		CompilingPackage newPackage = new CompilingPackage(pkg.getOrCreatePackage(name), module);
		packages.put(name, newPackage);
		return newPackage;
	}
	
	public void addPackage(String name, CompilingPackage package_) {
		packages.put(name, package_);
	}
	
	public void addType(String name, CompilingType type) {
		types.put(name, type);
	}
	
	public HighLevelDefinition getImport(TypeResolutionContext context, List<String> name) {
		return getImport(context, name, 0);
	}
	
	private HighLevelDefinition getImport(TypeResolutionContext context, List<String> name, int index) {
		if (packages.containsKey(name.get(index)))
			return packages.get(name.get(index)).getImport(context, name, index + 1);
		if (types.containsKey(name.get(index)))
			return getImportType(context, types.get(name.get(index)), name, index + 1);
		
		return null;
	}
	
	private HighLevelDefinition getImportType(TypeResolutionContext context, CompilingType type, List<String> name, int index) {
		if (index == name.size())
			return type.load();
		
		return getImportType(context, type.getInner(name.get(index)), name, index + 1);
	}
	
	public TypeID getType(TypeResolutionContext context, List<GenericName> name) {
		return getType(context, name, 0);
	}
	
	private TypeID getType(TypeResolutionContext context, List<GenericName> name, int index) {
		if (index == name.size())
			return null;
		
		if (packages.containsKey(name.get(index).name))
			return packages.get(name.get(index)).getType(context, name, index + 1);
		
		if (types.containsKey(name.get(index).name)) {
			CompilingType type = types.get(name.get(index).name);
			DefinitionTypeID result = context.getTypeRegistry().getForDefinition(type.load(), name.get(index).arguments);
			return getInner(context, name, index + 1, type, result);
		}
		
		return null;
	}
	
	private TypeID getInner(TypeResolutionContext context, List<GenericName> name, int index, CompilingType type, DefinitionTypeID result) {
		if (index == name.size())
			return result;
		
		CompilingType innerType = type.getInner(name.get(index).name);
		if (innerType == null)
			return null;
		
		DefinitionTypeID innerResult = context.getTypeRegistry().getForDefinition(innerType.load(), name.get(index).arguments, result);
		return getInner(context, name, index + 1, innerType, innerResult);
	}
}
