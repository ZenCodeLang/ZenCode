/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import stdlib.Strings;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionBuilder {
	public final CodePosition position;
	public final ExpressionScope scope;
	
	public ExpressionBuilder(CodePosition position, ExpressionScope scope) {
		this.position = position;
		this.scope = scope;
	}
	
	public Expression constructNew(String typename, Expression... arguments) {
		String[] nameParts = Strings.split(typename, '.');
		List<GenericName> name = new ArrayList<>();
		for (String namePart : nameParts)
			name.add(new GenericName(namePart));
		ITypeID type = scope.getType(position, name);
		if (type == null)
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_TYPE, "No such type: " + typename);
		
		return constructNew(type, arguments);
	}
	
	public Expression constructNew(ITypeID type, Expression... arguments) {
		DefinitionMemberGroup constructors = scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CONSTRUCTOR);
		List<ITypeID>[] predictedTypes = constructors.predictCallTypes(scope, scope.hints, arguments.length);
		CallArguments compiledArguments = new CallArguments(arguments);
		FunctionalMemberRef member = constructors.selectMethod(position, scope, compiledArguments, true, true);
		if (member == null)
			throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "No matching constructor found");
		if (!member.isConstructor())
			throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "COMPILER BUG: constructor is not a constructor");
		
		return new NewExpression(
				position,
				type,
				member,
				compiledArguments,
				compiledArguments.getNumberOfTypeArguments() == 0 ? member.header : member.header.fillGenericArguments(scope.getTypeRegistry(), compiledArguments.typeArguments));
	}
	
	public Expression not(Expression value) {
		TypeMembers members = scope.getTypeMembers(value.type);
		return members.getOrCreateGroup(OperatorType.NOT)
				.call(position, scope, value, CallArguments.EMPTY, false);
	}
}
