/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallTranslator;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class TranslatedOperatorMemberRef extends FunctionalMemberRef {
	private final CallTranslator translator;
	
	public TranslatedOperatorMemberRef(OperatorMember member, FunctionHeader header, CallTranslator translator) {
		super(member, header);
		
		this.translator = translator;
	}

	@Override
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return translator.translate(new CallTranslator.Call(position, target, instancedHeader, arguments, scope));
	}
}
