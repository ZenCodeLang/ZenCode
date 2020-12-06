/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.context.ModuleContext;

/**
 * @author Hoofdgebruiker
 */
public class EncodingModule {
	public final ModuleContext context;
	public final List<EncodingDefinition> definitions = new ArrayList<>();
	public final boolean withCode;
	private final Module module;

	public EncodingModule(Module module, ModuleContext context, boolean withCode) {
		this.module = module;
		this.context = context;
		this.withCode = withCode;
	}

	public String getName() {
		return module.name;
	}

	public void add(EncodingDefinition definition) {
		definitions.add(definition);
	}
}
