/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeExpansionRegistry {
	private final List<ExpansionDefinition> expansions = new ArrayList<>();
	
	public void register(ExpansionDefinition expansion) {
		expansions.add(expansion);
	}
}
