/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.InvalidStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModuleTypeResolutionContext implements TypeResolutionContext {
	private final GlobalTypeRegistry registry;
	private final Map<String, AnnotationDefinition> annotations = new HashMap<>();
	private final Map<String, StorageType> storageTypes = new HashMap<>();
	private final Map<String, ISymbol> globals;
	private final ZSPackage rootPackage;
	
	private final CompilingPackage rootCompiling;
	
	public ModuleTypeResolutionContext(
			GlobalTypeRegistry registry,
			AnnotationDefinition[] annotations,
			StorageType[] storageTypes,
			ZSPackage rootPackage,
			CompilingPackage rootCompiling,
			Map<String, ISymbol> globals)
	{
		this.registry = registry;
		this.rootPackage = rootPackage;
		this.rootCompiling = rootCompiling;
		this.globals = globals;
		
		for (AnnotationDefinition annotation : annotations)
			this.annotations.put(annotation.getAnnotationName(), annotation);
		for (StorageType storageType : storageTypes)
			this.storageTypes.put(storageType.getName(), storageType);
	}
	
	@Override
	public ZSPackage getRootPackage() {
		return rootPackage;
	}
	
	@Override
	public GlobalTypeRegistry getTypeRegistry() {
		return registry;
	}
	
	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return annotations.get(name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		if (rootCompiling != null) {
			TypeID compiling = rootCompiling.getType(this, name);
			if (compiling != null)
				return compiling;
		}
		
		if (name.size() == 1 && globals.containsKey(name.get(0).name))
			return globals.get(name.get(0).name).getType(position, this, name.get(0).arguments);
		
		return rootPackage.getType(position, this, name);
	}
	
	@Override
	public StorageTag getStorageTag(CodePosition position, String name, String[] arguments) {
		if (!storageTypes.containsKey(name))
			return new InvalidStorageTag(position, CompileExceptionCode.NO_SUCH_STORAGE_TYPE, "No such storage type: " + name);
		
		return storageTypes.get(name).instance(position, arguments);
	}
	
	@Override
	public StoredType getThisType() {
		return null;
	}
}
