package org.openzen.zenscript.codemodel.context;

import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CompilingPackage {
	public final ModuleSymbol module;
	private final ZSPackage pkg;
	private final Map<String, CompilingPackage> packages = new HashMap<>();
	private final Map<String, CompilingDefinition> types = new HashMap<>();

	public CompilingPackage(ZSPackage pkg, ModuleSymbol module) {
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

	public void addType(String name, CompilingDefinition type) {
		types.put(name, type);
	}

	public Optional<TypeID> getType(List<GenericName> name) {
		return Optional.ofNullable(getType(name, 0));
	}

	private TypeID getType( List<GenericName> name, int index) {
		if (index == name.size())
			return null;

		if (packages.containsKey(name.get(index).name))
			return packages.get(name.get(index).name).getType(name, index + 1);

		if (types.containsKey(name.get(index).name)) {
			CompilingDefinition type = types.get(name.get(index).name);
			TypeID result = DefinitionTypeID.create(type.getDefinition(), name.get(index).arguments);
			return getInner(name, index + 1, type, result);
		}

		return null;
	}

	private TypeID getInner(List<GenericName> name, int index, CompilingDefinition type, TypeID result) {
		if (index == name.size())
			return result;

		Optional<CompilingDefinition> innerType = type.getInner(name.get(index).name);
		if (!innerType.isPresent())
			return null;

		TypeID innerResult = DefinitionTypeID.create(innerType.get().getDefinition(), name.get(index).arguments);
		return getInner(name, index + 1, innerType.get(), innerResult);
	}
}
