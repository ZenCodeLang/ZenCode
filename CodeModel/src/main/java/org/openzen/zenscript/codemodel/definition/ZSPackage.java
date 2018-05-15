/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialPackageExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSPackage {
	public final String fullName;
	
	public ZSPackage(String fullName) {
		this.fullName = fullName;
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
			if (types.get(name.name).genericParameters.length != name.getNumberOfArguments())
				throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_INVALID_NUMBER, "Invalid number of type arguments");
			
			return new PartialTypeExpression(position, registry.getForDefinition(types.get(name.name), name.arguments));
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
	
	public ITypeID getType(CodePosition position, TypeScope scope, List<GenericName> nameParts) {
		return getType(position, scope, nameParts, 0);
	}
	
	public ITypeID getType(CodePosition position, TypeScope scope, GenericName name) {
		if (types.containsKey(name.name)) {
			return scope.getTypeRegistry().getForDefinition(types.get(name.name), name.arguments);
		}
		
		return null;
	}
	
	private ITypeID getType(CodePosition position, TypeScope scope, List<GenericName> nameParts, int depth) {
		if (depth >= nameParts.size())
			return null;
		
		GenericName name = nameParts.get(depth);
		if (subPackages.containsKey(name.name) && name.hasNoArguments())
			return subPackages.get(name.name).getType(position, scope, nameParts, depth + 1);
		
		if (types.containsKey(name.name)) {
			ITypeID type = scope.getTypeRegistry().getForDefinition(types.get(name.name), name.arguments);
			depth++;
			while (depth < nameParts.size()) {
				GenericName innerName = nameParts.get(depth++);
				type = scope.getTypeMembers(type).getInnerType(position, innerName);
				if (type == null)
					return null;
			}
			
			return type;
		}
		
		return null;
	}
	
	public ZSPackage getOrCreatePackage(String name) {
		if (subPackages.containsKey(name))
			return subPackages.get(name);
		
		ZSPackage result = new ZSPackage(fullName.isEmpty() ? name : fullName + '.' + name);
		subPackages.put(name, result);
		return result;
	}
	
	public void register(HighLevelDefinition definition) {
		types.put(definition.name, definition);
	}
}
