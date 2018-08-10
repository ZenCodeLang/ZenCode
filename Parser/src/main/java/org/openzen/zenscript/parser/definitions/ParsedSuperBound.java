/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedSuperBound extends ParsedGenericBound {
	public final IParsedType type;
	
	public ParsedSuperBound(IParsedType type) {
		this.type = type;
	}

	@Override
	public GenericParameterBound compile(TypeResolutionContext context) {
		return new ParameterSuperBound(type.compile(context));
	}
}
