package org.openzen.zenscript.codemodel.context;

import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;

import java.util.List;

public class ModuleContext {
	public final ModuleSymbol module;
	public final List<ExpansionSymbol> expansions;
	public final ZSPackage root;

	public ModuleContext(
			ModuleSymbol module,
			List<ExpansionSymbol> expansions,
			ZSPackage root) {
		this.module = module;
		this.expansions = expansions;
		this.root = root;
	}
}
