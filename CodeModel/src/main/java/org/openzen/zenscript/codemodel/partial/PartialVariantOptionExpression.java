package org.openzen.zenscript.codemodel.partial;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.VariantValueExpression;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PartialVariantOptionExpression implements IPartialExpression {
	private final CodePosition position;
	private final VariantOptionRef option;

	public PartialVariantOptionExpression(CodePosition position, VariantOptionRef option) {
		this.position = position;
		this.option = option;
	}

	@Override
	public Expression eval() {
		return new InvalidExpression(position, option.variant, CompileExceptionCode.VARIANT_OPTION_NOT_AN_EXPRESSION, "Cannot use a variant option as expression");
	}

	@Override
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) {
		if (arguments != option.getOption().types.length)
			return new List[0];

		return new List[]{Arrays.asList(option.getOption().types)};
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) {
		if (arguments != option.getOption().types.length)
			return Collections.emptyList();

		return Collections.singletonList(new FunctionHeader(option.variant, option.types));
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Variant options don't have members");
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) {
		return new VariantValueExpression(position, option.variant, option, arguments.arguments);
	}

	@Override
	public TypeID[] getTypeArguments() {
		return TypeID.NONE;
	}
}
