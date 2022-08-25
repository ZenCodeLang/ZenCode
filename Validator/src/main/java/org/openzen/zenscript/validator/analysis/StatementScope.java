/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.analysis;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;

/**
 * @author Hoofdgebruiker
 */
public interface StatementScope {
	boolean isConstructor();

	boolean isStatic();

	FunctionHeader getFunctionHeader();

	boolean isStaticInitializer();

	HighLevelDefinition getDefinition();
}
