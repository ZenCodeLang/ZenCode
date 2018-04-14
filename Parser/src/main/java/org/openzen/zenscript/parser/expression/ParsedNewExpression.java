/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.ICallableMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedNewExpression extends ParsedExpression{
	private final IParsedType type;
	private final ParsedCallArguments arguments;
	
	public ParsedNewExpression(CodePosition position, IParsedType type, ParsedCallArguments arguments) {
		super(position);
		
		this.type = type;
		this.arguments = arguments;
	}
	
	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID type = this.type.compile(scope);
		return compile(position, type, arguments, scope);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
	
	public static Expression compile(CodePosition position, ITypeID type, ParsedCallArguments arguments, ExpressionScope scope) {
		DefinitionMemberGroup constructors = scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CONSTRUCTOR);
		List<ITypeID>[] predictedTypes = constructors.predictCallTypes(scope, scope.hints, arguments.arguments.size());
		CallArguments compiledArguments = arguments.compileCall(position, scope, constructors);
		ICallableMember member = constructors.selectMethod(position, scope, compiledArguments, true, true);
		if (member == null)
			throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "No matching constructor found");
		if (!(member instanceof ConstructorMember))
			throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "COMPILER BUG: constructor is not a constructor");
		
		return new NewExpression(position, type, (ConstructorMember) member, compiledArguments);
	}
}
