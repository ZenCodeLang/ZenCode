/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallTranslator;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class TranslatedOperatorMember extends OperatorMember {
	private final CallTranslator translator;
	
	public TranslatedOperatorMember(CodePosition position, int modifiers, OperatorType operator, FunctionHeader header, CallTranslator translator) {
		super(position, modifiers, operator, header);
		
		this.translator = translator;
	}

	@Override
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments) {
		return translator.translate(new CallTranslator.Call(position, target, instancedHeader, arguments));
	}
}