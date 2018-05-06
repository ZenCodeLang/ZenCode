/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.analysis;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.statement.VarStatement;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ExpressionScope {
	public boolean isConstructor();
	
	public boolean isFirstStatement();
	
	public boolean hasThis();
	
	public boolean isFieldInitialized(FieldMember field);
	
	public boolean isEnumConstantInitialized(EnumConstantMember member);
	
	public boolean isLocalVariableInitialized(VarStatement variable);
	
	public void markConstructorForwarded();
	
	public boolean isStaticInitializer();
	
	public HighLevelDefinition getDefinition();
}
