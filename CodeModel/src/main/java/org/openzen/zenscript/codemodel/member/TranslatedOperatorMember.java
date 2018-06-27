/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallTranslator;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class TranslatedOperatorMember extends OperatorMember {
	private final CallTranslator translator;
	
	public TranslatedOperatorMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			OperatorType operator,
			FunctionHeader header,
			CallTranslator translator,
			BuiltinID builtin)
	{
		super(position, definition, modifiers, operator, header, builtin);
		
		this.translator = translator;
	}

	@Override
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return translator.translate(new CallTranslator.Call(position, target, instancedHeader, arguments, scope));
	}
}
