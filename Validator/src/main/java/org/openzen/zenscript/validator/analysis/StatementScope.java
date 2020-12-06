/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.analysis;

import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;

/**
 * @author Hoofdgebruiker
 */
public interface StatementScope {
	public boolean isConstructor();

	public boolean isStatic();

	public FunctionHeader getFunctionHeader();

	public boolean isStaticInitializer();

	public HighLevelDefinition getDefinition();

	public AccessScope getAccessScope();
}
