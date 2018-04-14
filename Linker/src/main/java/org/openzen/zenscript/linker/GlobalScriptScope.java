/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.linker;

import java.util.List;
import java.util.function.Function;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalScriptScope extends StatementScope {
	private final FileScope file;
	
	public GlobalScriptScope(FileScope file) {
		this.file = file;
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return file.getMemberCache();
	}
	
	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		IPartialExpression result = super.get(position, name);
		if (result != null)
			return result;
		
		return file.get(position, name);
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		return file.getType(position, name);
	}

	@Override
	public Statement getLoop(String name) {
		return null;
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return null;
	}

	@Override
	public ITypeID getThisType() {
		return null;
	}

	@Override
	public Function<CodePosition, Expression> getDollar() {
		return null; // script arguments?
	}

	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		return null;
	}
}
