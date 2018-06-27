/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.ConstructorThisCallExpression;
import org.openzen.zenscript.codemodel.expression.VariantValueExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.ICallableMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionCall extends ParsedExpression {
	private final ParsedExpression receiver;
	private final ParsedCallArguments arguments;

	public ParsedExpressionCall(CodePosition position, ParsedExpression receiver, ParsedCallArguments arguments) {
		super(position);

		this.receiver = receiver;
		this.arguments = arguments;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		if (receiver instanceof ParsedExpressionVariable) {
			ParsedExpressionVariable variable = (ParsedExpressionVariable) receiver;
			for (ITypeID hint : scope.hints) {
				TypeMembers members = scope.getTypeMembers(hint);
				if (members.getVariantOption(variable.name) != null) {
					VariantDefinition.Option variantOption = members.getVariantOption(variable.name);
					FunctionHeader header = new FunctionHeader(BasicTypeID.VOID, variantOption.types);
					CallArguments cArguments = arguments.compileCall(position, scope, null, header);
					return new VariantValueExpression(position, hint, variantOption, cArguments.arguments);
				}
			}
		}
		
		if (receiver instanceof ParsedExpressionSuper) {
			// super call (intended as first call in constructor)
			ITypeID targetType = scope.getThisType().getSuperType();
			if (targetType == null)
				throw new CompileException(position, CompileExceptionCode.SUPER_CALL_NO_SUPERCLASS, "Class has no superclass");
			
			DefinitionMemberGroup memberGroup = scope.getTypeMembers(targetType).getOrCreateGroup(OperatorType.CONSTRUCTOR);
			CallArguments callArguments = arguments.compileCall(position, scope, null, memberGroup);
			ICallableMember member = memberGroup.selectMethod(position, scope, callArguments, true, true);
			if (!(member instanceof ConstructorMember))
				throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Constructor is not a constructor!");
			
			return new ConstructorThisCallExpression(position, scope.getThisType().getSuperType(), (ConstructorMember) member, callArguments, scope);
		} else if (receiver instanceof ParsedExpressionThis) {
			// this call (intended as first call in constructor)
			ITypeID targetType = scope.getThisType();
			
			DefinitionMemberGroup memberGroup = scope.getTypeMembers(targetType).getOrCreateGroup(OperatorType.CONSTRUCTOR);
			CallArguments callArguments = arguments.compileCall(position, scope, null, memberGroup);
			ICallableMember member = memberGroup.selectMethod(position, scope, callArguments, true, true);
			if (!(member instanceof ConstructorMember))
				throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Constructor is not a constructor!");
			
			return new ConstructorThisCallExpression(position, scope.getThisType(), (ConstructorMember) member, callArguments, scope);
		}
		
		IPartialExpression cReceiver = receiver.compile(scope.withoutHints());
		List<FunctionHeader> headers = cReceiver.getPossibleFunctionHeaders(scope, scope.hints, arguments.arguments.size());
		CallArguments callArguments = arguments.compileCall(position, scope, cReceiver.getGenericCallTypes(), headers);
		return cReceiver.call(position, scope, scope.hints, callArguments);
	}
	
	@Override
	public SwitchValue compileToSwitchValue(ITypeID type, ExpressionScope scope) {
		if (!(receiver instanceof ParsedExpressionVariable))
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");
		
		String name = ((ParsedExpressionVariable)receiver).name;
		TypeMembers members = scope.getTypeMembers(type);
		if (type.isVariant()) {
			VariantDefinition.Option option = members.getVariantOption(name);
			if (option == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Variant option does not exist: " + name);
			
			VarStatement[] values = new VarStatement[arguments.arguments.size()];
			for (int i = 0; i < values.length; i++) {
				ParsedExpression argument = arguments.arguments.get(i);
				ParsedFunctionParameter lambdaHeader = argument.toLambdaParameter();
				values[i] = new VarStatement(argument.position, lambdaHeader.name, lambdaHeader.type.compile(scope), null, true);
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
