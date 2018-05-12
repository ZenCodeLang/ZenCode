/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Arrays;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallExpression extends Expression {
	public final Expression target;
	public final FunctionalMember member;
	public final CallArguments arguments;
	public final FunctionHeader instancedHeader;
	
	public CallExpression(CodePosition position, Expression target, FunctionalMember member, FunctionHeader instancedHeader, CallArguments arguments) {
		super(position, instancedHeader.returnType);
		
		if (arguments.arguments.length < instancedHeader.parameters.length) {
			Expression[] newArguments = Arrays.copyOf(arguments.arguments, instancedHeader.parameters.length);
			for (int i = arguments.arguments.length; i < instancedHeader.parameters.length; i++) {
				if (instancedHeader.parameters[i].defaultValue == null)
					throw new CompileException(position, CompileExceptionCode.MISSING_PARAMETER, "Parameter missing and no default value specified");
				newArguments[i] = instancedHeader.parameters[i].defaultValue;
			}
			arguments = new CallArguments(arguments.typeArguments, newArguments);
		}
		
		this.target = target;
		this.member = member;
		this.arguments = arguments;
		this.instancedHeader = instancedHeader;
	}
	
	public Expression getFirstArgument() {
		return arguments.arguments[0];
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCall(this);
	}
}
