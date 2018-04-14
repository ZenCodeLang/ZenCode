/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.linker.BaseScope;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class ParsedGenericBound {
	public abstract GenericParameterBound compile(BaseScope scope);
}
