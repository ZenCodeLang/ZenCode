package org.openzen.zenscript.codemodel.context;

import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;

import java.util.List;

public class ModuleContext {
	public final Module module;
	public final List<ExpansionDefinition> expansions;
	public final ZSPackage root;

	public ModuleContext(
			Module module,
			List<ExpansionDefinition> expansions,
			ZSPackage root) {
		this.module = module;
		this.expansions = expansions;
		this.root = root;
	}
}
