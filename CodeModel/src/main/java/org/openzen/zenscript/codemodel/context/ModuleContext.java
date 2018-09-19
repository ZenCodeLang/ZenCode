/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.List;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModuleContext {
	public final GlobalTypeRegistry registry;
	public final Module module;
	public final List<ExpansionDefinition> expansions;
	public final ZSPackage root;
	
	public ModuleContext(
			GlobalTypeRegistry registry,
			Module module,
			List<ExpansionDefinition> expansions,
			ZSPackage root)
	{
		this.registry = registry;
		this.module = module;
		this.expansions = expansions;
		this.root = root;
	}
}
