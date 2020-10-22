/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFunctionType implements IParsedType {
	private final ParsedFunctionHeader header;

	public ParsedFunctionType(ParsedFunctionHeader header) {
		this.header = header;
	}

	@Override
	public TypeID compile(TypeResolutionContext context) {
		return context.getTypeRegistry().getFunction(header.compile(context));
	}
}
