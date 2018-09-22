/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialPackageExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSPackage {
	public static ZSPackage createRoot() {
		return new ZSPackage(null, "");
	}
	
	public final String name;
	public final String fullName;
	public final ZSPackage parent;
	
	public ZSPackage(ZSPackage parent, String name) {
		this.parent = parent;
		this.name = name;
		this.fullName = parent == null ? name : parent.fullName + "." + name;
	}
	
	private final Map<String, ZSPackage> subPackages = new HashMap<>();
	private final Map<String, HighLevelDefinition> types = new HashMap<>();
	
	public void add(String name, ZSPackage subPackage) {
		if (subPackages.containsKey(name))
			throw new RuntimeException("Such package already exists: " + name);
		
		subPackages.put(name, subPackage);
	}
	
	public IPartialExpression getMember(CodePosition position, GlobalTypeRegistry registry, GenericName name) {
		if (subPackages.containsKey(name.name) && name.hasNoArguments())
			return new PartialPackageExpression(position, subPackages.get(name.name));
		
		if (types.containsKey(name.name)) {
			if (types.get(name.name).typeParameters.length != name.getNumberOfArguments())
				throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_INVALID_NUMBER, "Invalid number of type arguments");
			
			return new PartialTypeExpression(position, registry.getForDefinition(types.get(name.name), null, name.arguments), name.arguments);
		}
		
		return null;
	}
	
	public boolean contains(String name) {
		return types.containsKey(name) || subPackages.containsKey(name);
	}
	
	public HighLevelDefinition getDefinition(String name) {
		return types.get(name);
	}
	
	public HighLevelDefinition getImport(List<String> name, int depth) {
		if (depth >= name.size())
			return null;
		
		if (subPackages.containsKey(name.get(depth)))
			return subPackages.get(name.get(depth)).getImport(name, depth + 1);
		
		if (depth == name.size() - 1 && types.containsKey(name.get(depth)))
			return types.get(name.get(depth));
		
		return null;
	}
	
	public ITypeID getType(CodePosition position, TypeResolutionContext context, List<GenericName> nameParts, StorageTag storage) {
		return getType(position, context, nameParts, 0, storage);
	}
	
	public ITypeID getType(CodePosition position, TypeResolutionContext context, GenericName name, StorageTag storage) {
		if (types.containsKey(name.name)) {
			return context.getTypeRegistry().getForDefinition(types.get(name.name), storage, name.arguments);
		}
		
		return null;
	}
	
	private ITypeID getType(CodePosition position, TypeResolutionContext context, List<GenericName> nameParts, int depth, StorageTag storage) {
		if (depth >= nameParts.size())
			return null;
		
		GenericName name = nameParts.get(depth);
		if (subPackages.containsKey(name.name) && name.hasNoArguments())
			return subPackages.get(name.name).getType(position, context, nameParts, depth + 1, storage);
		
		if (types.containsKey(name.name)) {
			DefinitionTypeID type = context.getTypeRegistry().getForDefinition(types.get(name.name), null, name.arguments);
			return GenericName.getInnerType(context.getTypeRegistry(), type, nameParts, depth + 1, storage);
		}
		
		return null;
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
	
	public void register(HighLevelDefinition definition) {
		types.put(definition.name, definition);
	}
}
