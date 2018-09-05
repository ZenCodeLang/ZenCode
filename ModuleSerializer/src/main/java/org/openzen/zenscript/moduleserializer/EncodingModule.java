/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.Module;

/**
 *
 * @author Hoofdgebruiker
 */
public class EncodingModule {
	private final Module module;
	public final List<EncodingDefinition> definitions = new ArrayList<>();
	public final boolean withCode;
	
	public EncodingModule(Module module, boolean withCode) {
		this.module = module;
		this.withCode = withCode;
	}
	
	public String getName() {
		return module.name;
	}
	
	public void add(EncodingDefinition definition) {
		definitions.add(definition);
	}
}
