/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.VariantValueExpression;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialVariantOptionExpression implements IPartialExpression {
	private final CodePosition position;
	private final TypeScope scope;
	private final VariantOptionRef option;
	
	public PartialVariantOptionExpression(CodePosition position, TypeScope scope, VariantOptionRef option) {
		this.position = position;
		this.scope = scope;
		this.option = option;
	}
	
	@Override
	public Expression eval() {
		throw new CompileException(position, CompileExceptionCode.VARIANT_OPTION_NOT_AN_EXPRESSION, "Cannot use a variant option as expression");
	}

	@Override
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
		if (arguments != option.getOption().types.length)
			return new List[0];
		
		return new List[] { Arrays.asList(option.getOption().types) };
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
		if (arguments != option.getOption().types.length)
			return Collections.emptyList();
		
		return Collections.singletonList(new FunctionHeader(option.variant, option.types));
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
		throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Variant options don't have members");
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		return new VariantValueExpression(position, option.variant, option, arguments.arguments);
	}

	@Override
	public ITypeID[] getGenericCallTypes() {
		return ITypeID.NONE;
	}
}
