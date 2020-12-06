package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;

import java.util.List;

public class ParsedExpressionCall extends ParsedExpression {
	private final ParsedExpression receiver;
	private final ParsedCallArguments arguments;

	public ParsedExpressionCall(CodePosition position, ParsedExpression receiver, ParsedCallArguments arguments) {
		super(position);

		this.receiver = receiver;
		this.arguments = arguments;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		if (receiver instanceof ParsedExpressionVariable) {
			ParsedExpressionVariable variable = (ParsedExpressionVariable) receiver;
			for (TypeID hint : scope.hints) {
				TypeMembers members = scope.getTypeMembers(hint);
				if (members.getVariantOption(variable.name) != null) {
					try {
						VariantOptionRef variantOption = members.getVariantOption(variable.name);
						FunctionHeader header = new FunctionHeader(BasicTypeID.VOID, variantOption.types);
						CallArguments cArguments = arguments.compileCall(position, scope, null, header);
						return new VariantValueExpression(position, hint, variantOption, cArguments.arguments);
					} catch (CompileException ex) {
						return new InvalidExpression(hint, ex);
					}
				}
			}
		}

		if (receiver instanceof ParsedExpressionSuper) {
			// super call (intended as first call in constructor)
			TypeID targetType = scope.getThisType().getSuperType(scope.getTypeRegistry());
			if (targetType == null)
				throw new CompileException(position, CompileExceptionCode.SUPER_CALL_NO_SUPERCLASS, "Class has no superclass");

			TypeMemberGroup memberGroup = scope.getTypeMembers(targetType).getOrCreateGroup(OperatorType.CONSTRUCTOR);
			CallArguments callArguments = arguments.compileCall(position, scope, null, memberGroup);
			FunctionalMemberRef member = memberGroup.selectMethod(position, scope, callArguments, true, true);
			if (!member.isConstructor())
				throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Constructor is not a constructor!");

			return new ConstructorSuperCallExpression(position, targetType, member, callArguments);
		} else if (receiver instanceof ParsedExpressionThis) {
			// this call (intended as first call in constructor)
			TypeID targetType = scope.getThisType();

			TypeMemberGroup memberGroup = scope.getTypeMembers(targetType).getOrCreateGroup(OperatorType.CONSTRUCTOR);
			CallArguments callArguments = arguments.compileCall(position, scope, null, memberGroup);
			FunctionalMemberRef member = memberGroup.selectMethod(position, scope, callArguments, true, true);
			if (!member.isConstructor())
				throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Constructor is not a constructor!");

			return new ConstructorThisCallExpression(position, scope.getThisType(), member, callArguments);
		}

		IPartialExpression cReceiver = receiver.compile(scope.withoutHints());
		List<FunctionHeader> headers = cReceiver.getPossibleFunctionHeaders(scope, scope.hints, arguments.arguments.size());
		CallArguments callArguments = arguments.compileCall(position, scope, cReceiver.getTypeArguments(), headers);
		return cReceiver.call(position, scope, scope.hints, callArguments);
	}

	@Override
	public SwitchValue compileToSwitchValue(TypeID type, ExpressionScope scope) throws CompileException {
		if (!(receiver instanceof ParsedExpressionVariable))
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");

		String name = ((ParsedExpressionVariable) receiver).name;
		TypeMembers members = scope.getTypeMembers(type);
		if (type.isVariant()) {
			VariantOptionRef option = members.getVariantOption(name);
			if (option == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Variant option does not exist: " + name);

			String[] values = new String[arguments.arguments.size()];
			for (int i = 0; i < values.length; i++) {
				try {
					ParsedExpression argument = arguments.arguments.get(i);
					ParsedFunctionParameter lambdaHeader = argument.toLambdaParameter();
					values[i] = lambdaHeader.name;
				} catch (ParseException ex) {
					throw new CompileException(ex.position, CompileExceptionCode.INVALID_SWITCH_CASE, ex.getMessage());
				}
			}

			return new VariantOptionSwitchValue(option, values);
		} else {
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");
		}
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
