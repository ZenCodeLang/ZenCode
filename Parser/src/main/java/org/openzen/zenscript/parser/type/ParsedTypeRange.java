/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeRange implements IParsedType {
	private final CodePosition position;
	private final IParsedType from;
	private final IParsedType to;
	private final int modifiers;
	
	public ParsedTypeRange(CodePosition position, IParsedType from, IParsedType to) {
		this.position = position;
		this.from = from;
		this.to = to;
		this.modifiers = 0;
	}
	
	private ParsedTypeRange(CodePosition position, IParsedType from, IParsedType to, int modifiers) {
		this.position = position;
		this.from = from;
		this.to = to;
		this.modifiers = modifiers;
	}

	@Override
	public IParsedType withOptional() {
		return new ParsedTypeRange(position, from, to, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeRange(position, from, to, this.modifiers | modifiers);
	}
	
	@Override
	public ITypeID compile(TypeResolutionContext context) {
		ITypeID from = this.from.compile(context);
		ITypeID to = this.to.compile(context);
		if (from != to)
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_TYPE, "from and to in a range must be the same type");
		
		return context.getTypeRegistry().getModified(modifiers, context.getTypeRegistry().getRange(from));
	}
}
