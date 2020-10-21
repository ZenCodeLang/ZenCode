/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedStorageTag;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionCast extends ParsedExpression {
	private final ParsedExpression value;
	private final IParsedType type;
	private final boolean optional;

	public ParsedExpressionCast(CodePosition position, ParsedExpression value, IParsedType type, boolean optional) {
		super(position);

		this.value = value;
		this.type = type;
		this.optional = optional;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		TypeID type = this.type.compile(scope);
		return value.compile(scope.withHint(type))
				.eval()
				.castExplicit(position, scope, type, optional);
	}
	
	@Override
	public ParsedFunctionHeader toLambdaHeader() throws ParseException {
		if (optional)
			throw new ParseException(position, "Not a valid lambda header");
		
		ParsedFunctionHeader header = value.toLambdaHeader();
		if (header.returnType != ParsedTypeBasic.UNDETERMINED)
			throw new ParseException(position, "Lambda parameter already has a return type");
		
		return new ParsedFunctionHeader(position, header.genericParameters, header.parameters, type, null, ParsedStorageTag.NULL);
	}
	
	@Override
	public ParsedFunctionParameter toLambdaParameter() throws ParseException {
		if (optional)
			throw new ParseException(position, "Not a valid lambda header");
		
		ParsedFunctionParameter parameter = value.toLambdaParameter();
		if (parameter.type != ParsedTypeBasic.UNDETERMINED)
			throw new ParseException(position, "Lambda parameter already has a type");
		
		return new ParsedFunctionParameter(ParsedAnnotation.NONE, parameter.name, type, null, false);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
