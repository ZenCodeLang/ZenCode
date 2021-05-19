package org.openzen.zenscript.codemodel.context;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileResolutionContext implements TypeResolutionContext {
	private final ModuleTypeResolutionContext module;
	private final CompilingPackage modulePackage;
	private final Map<String, HighLevelDefinition> imports = new HashMap<>();
	private final ZSPackage root;

	public FileResolutionContext(ModuleTypeResolutionContext module, ZSPackage root, CompilingPackage modulePackage) {
		this.module = module;
		this.root = root;
		this.modulePackage = modulePackage;
	}

	public void addImport(String name, HighLevelDefinition definition) {
		if (definition == null)
			throw new NullPointerException();

		imports.put(name, definition);
	}

	@Override
	public ZSPackage getRootPackage() {
		return root;
	}

	@Override
	public GlobalTypeRegistry getTypeRegistry() {
		return module.getTypeRegistry();
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return module.getAnnotation(name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		if (imports.containsKey(name.get(0).name)) {
			HighLevelDefinition definition = imports.get(name.get(0).name);
			if (definition.getNumberOfGenericParameters() != name.get(0).getNumberOfArguments())
				return new InvalidTypeID(position, CompileExceptionCode.INVALID_TYPE_ARGUMENTS, "Invalid number of type arguments");

			return GenericName.getInnerType(
					getTypeRegistry(),
					getTypeRegistry().getForDefinition(definition, name.get(0).arguments),
					name,
					1);
		}

		final TypeID typeFromRootAndParents = getTypeFromRootAndParents(root, position, name);
		if (typeFromRootAndParents != null) {
			return typeFromRootAndParents;
		}

		TypeID moduleType = modulePackage.getType(this, name);
		if (moduleType != null)
			return moduleType;

		return module.getType(position, name);
	}

	private TypeID getTypeFromRootAndParents(ZSPackage root, CodePosition position, List<GenericName> name) {
		if (root.contains(name.get(0).name)) {
			return root.getType(position, this, name);
		} else if(root.name.equals(name.get(0).name)) {
			return root.getType(position, this, name.subList(1, name.size()));
		}

		if(root.parent != null) {
			return getTypeFromRootAndParents(root.parent, position, name);
		}
		return null;
	}

	@Override
	public TypeID getThisType() {
		return null;
	}
}
