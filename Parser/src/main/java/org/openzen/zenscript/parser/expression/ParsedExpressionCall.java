/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.ConstructorCallExpression;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.ICallableMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.linker.ExpressionScope;
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
		IPartialExpression cReceiver = receiver.compile(scope.withoutHints());
		
		if (receiver instanceof ParsedExpressionSuper) {
			// super call (intended as first call in constructor)
			ITypeID targetType = scope.getThisType().getSuperType();
			if (targetType == null)
				throw new CompileException(position, CompileExceptionCode.SUPER_CALL_NO_SUPERCLASS, "Class has no superclass");
			
			DefinitionMemberGroup memberGroup = scope.getTypeMembers(targetType).getOrCreateGroup(OperatorType.CONSTRUCTOR);
			CallArguments callArguments = arguments.compileCall(position, scope, memberGroup);
			ICallableMember member = memberGroup.selectMethod(position, scope, callArguments, true, true);
			if (!(member instanceof ConstructorMember))
				throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Constructor is not a constructor!");
			
			return new ConstructorCallExpression(position, scope.getThisType().getSuperType(), (ConstructorMember) member, callArguments);
		} else if (receiver instanceof ParsedExpressionThis) {
			// this call (intended as first call in constructor)
			ITypeID targetType = scope.getThisType();
			
			DefinitionMemberGroup memberGroup = scope.getTypeMembers(targetType).getOrCreateGroup(OperatorType.CONSTRUCTOR);
			CallArguments callArguments = arguments.compileCall(position, scope, memberGroup);
			ICallableMember member = memberGroup.selectMethod(position, scope, callArguments, true, true);
			if (!(member instanceof ConstructorMember))
				throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Constructor is not a constructor!");
			
			return new ConstructorCallExpression(position, scope.getThisType().getSuperType(), (ConstructorMember) member, callArguments);
		}

		List<FunctionHeader> headers = cReceiver.getPossibleFunctionHeaders(scope, scope.hints, arguments.arguments.size());
		CallArguments callArguments = arguments.compileCall(position, scope, headers);
		return cReceiver.call(position, scope, scope.hints, callArguments);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
