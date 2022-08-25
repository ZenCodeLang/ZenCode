package org.openzen.zenscript.codemodel.context;

import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			return packages.get(name.get(index).name).getType(context, name, index + 1);

		if (types.containsKey(name.get(index).name)) {
			CompilingType type = types.get(name.get(index).name);
			TypeID result = DefinitionTypeID.create(type.load(), name.get(index).arguments);
			return getInner(context, name, index + 1, type, result);
		}

		return null;
	}

	private TypeID getInner(TypeResolutionContext context, List<GenericName> name, int index, CompilingType type, TypeID result) {
		if (index == name.size())
			return result;

		CompilingType innerType = type.getInner(name.get(index).name);
		if (innerType == null)
			return null;

		TypeID innerResult = DefinitionTypeID.create(innerType.load(), name.get(index).arguments);
		return getInner(context, name, index + 1, innerType, innerResult);
	}
}
