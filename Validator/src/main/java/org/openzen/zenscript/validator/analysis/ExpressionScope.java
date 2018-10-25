/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.analysis;

import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.statement.VarStatement;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ExpressionScope {
	boolean isConstructor();
	
	boolean isFirstStatement();
	
	boolean hasThis();
	
	boolean isFieldInitialized(FieldMember field);
	
	boolean isEnumConstantInitialized(EnumConstantMember member);
	
	boolean isLocalVariableInitialized(VarStatement variable);
	
	void markConstructorForwarded();
	
	boolean isStaticInitializer();
	
	HighLevelDefinition getDefinition();
	
	AccessScope getAccessScope();
}
