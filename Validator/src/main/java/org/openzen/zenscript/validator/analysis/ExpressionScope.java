/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.analysis;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;

/**
 * @author Hoofdgebruiker
 */
public interface ExpressionScope {
	boolean isConstructor();

	boolean isFirstStatement();

	boolean hasThis();

	boolean isFieldInitialized(FieldSymbol field);

	boolean isEnumConstantInitialized(EnumConstantMember member);

	boolean isLocalVariableInitialized(VariableID variable);

	void markConstructorForwarded();

	boolean isStaticInitializer();

	HighLevelDefinition getDefinition();
}
