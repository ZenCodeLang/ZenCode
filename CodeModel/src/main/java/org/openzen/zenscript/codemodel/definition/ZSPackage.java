package org.openzen.zenscript.codemodel.definition;

import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ZSPackage {
	public final String name;
	public final String fullName;
	public final ZSPackage parent;
	private final Map<String, ZSPackage> subPackages = new HashMap<>();
	private final Map<String, TypeSymbol> types = new HashMap<>();

	public ZSPackage(ZSPackage parent, String name) {
		this.parent = parent;
		this.name = name;
		this.fullName = parent == null || parent.fullName.isEmpty() ? name : parent.fullName + "." + name;
	}

	public static ZSPackage createRoot() {
		return new ZSPackage(null, "");
	}

	public void add(String name, ZSPackage subPackage) {
		if (subPackages.containsKey(name))
			throw new RuntimeException("Such package already exists: " + name);

		subPackages.put(name, subPackage);
	}

	public boolean contains(String name) {
		return types.containsKey(name) || subPackages.containsKey(name);
	}

	public TypeSymbol getImport(List<String> name, int depth) {
		if (depth >= name.size())
			return null;

		if (subPackages.containsKey(name.get(depth)))
			return subPackages.get(name.get(depth)).getImport(name, depth + 1);

		if (depth == name.size() - 1 && types.containsKey(name.get(depth)))
			return types.get(name.get(depth));

		return null;
	}

	public Optional<TypeID> getType(List<GenericName> nameParts) {
		return getType(nameParts, 0);
	}

	public Optional<TypeID> getType(GenericName name) {
		if (types.containsKey(name.name)) {
			return Optional.of(DefinitionTypeID.create(types.get(name.name), name.arguments));
		} else {
			return Optional.empty();
		}
	}

	private Optional<TypeID> getType(List<GenericName> nameParts, int depth) {
		if (depth >= nameParts.size())
			return Optional.empty();

		GenericName name = nameParts.get(depth);
		if (subPackages.containsKey(name.name) && name.hasNoArguments())
			return subPackages.get(name.name).getType(nameParts, depth + 1);

		if (types.containsKey(name.name)) {
			TypeID type = DefinitionTypeID.create(types.get(name.name), name.arguments);
			return GenericName.getInnerType(type, nameParts, depth + 1);
		}

		return Optional.empty();
	}

	public Optional<ZSPackage> getOptionalRecursive(String name) {
		int dot = name.indexOf('.');
		if (dot < 0)
			return getOptional(name);
		else
			return getOptionalRecursive(name.substring(0, dot)).flatMap(pkg -> pkg.getOptionalRecursive(name.substring(dot + 1)));
	}

	public Optional<ZSPackage> getOptional(String name) {
		return Optional.ofNullable(subPackages.get(name));
	}

	public ZSPackage getRecursive(String name) {
		int dot = name.indexOf('.');
		if (dot < 0)
			return getOrCreatePackage(name);
		else
			return getOrCreatePackage(name.substring(0, dot)).getRecursive(name.substring(dot + 1));
	}

	public ZSPackage getOrCreatePackage(String name) {
		if (subPackages.containsKey(name))
			return subPackages.get(name);

		ZSPackage result = new ZSPackage(this, name);
		subPackages.put(name, result);
		return result;
	}

	public void register(TypeSymbol definition) {
		types.put(definition.getName(), definition);
	}

	public ZSPackage getRoot() {
		if (this.parent == null) {
			return this;
		}
		return this.parent.getRoot();
	}
}
