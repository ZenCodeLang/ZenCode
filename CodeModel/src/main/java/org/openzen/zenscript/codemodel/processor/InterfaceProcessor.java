/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.processor;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ImplementationMember;

/**
 *
 * @author Hoofdgebruiker
 */
public interface InterfaceProcessor {
	public void apply(ImplementationMember implementation);

	public void applyOnSubclass(HighLevelDefinition definition, ImplementationMember implementation);
}
