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
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModuleTypeResolutionContext implements TypeResolutionContext {
	private final GlobalTypeRegistry registry;
	private final Map<String, AnnotationDefinition> annotations = new HashMap<>();
	private final Map<String, ISymbol> globals;
	private final ZSPackage rootPackage;
	
	private final CompilingPackage rootCompiling;
	
	public ModuleTypeResolutionContext(
			GlobalTypeRegistry registry,
			AnnotationDefinition[] annotations,
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
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		if (rootCompiling != null) {
			ITypeID compiling = rootCompiling.getType(this, name);
			if (compiling != null)
				return compiling;
		}
		
		if (name.size() == 1 && globals.containsKey(name.get(0).name))
			return globals.get(name.get(0).name).getType(position, this, name.get(0).arguments);
		
		return rootPackage.getType(position, this, name);
	}
	
	@Override
	public ITypeID getThisType() {
		return null;
	}
}
