/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

/**
 *
 * @author Hoofdgebruiker
 */
public class InvalidMemberAnnotation implements MemberAnnotation {
	public final CodePosition position;
	public final CompileExceptionCode code;
	public final String message;
	
	public InvalidMemberAnnotation(CodePosition position, CompileExceptionCode code, String message) {
		this.position = position;
		this.code = code;
		this.message = message;
	}
	
	public InvalidMemberAnnotation(CompileException ex) {
		this.position = ex.position;
		this.code = ex.code;
		this.message = ex.getMessage();
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return InvalidAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply(IDefinitionMember member, BaseScope scope) {
		
	}

	@Override
	public void applyOnOverridingMethod(FunctionalMember member, BaseScope scope) {
		
	}

	@Override
	public void applyOnOverridingGetter(GetterMember member, BaseScope scope) {
		
	}

	@Override
	public void applyOnOverridingSetter(SetterMember member, BaseScope scope) {
		
	}

	@Override
	public void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
