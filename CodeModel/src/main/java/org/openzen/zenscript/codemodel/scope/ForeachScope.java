/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ForeachScope extends StatementScope {
	private final StatementScope outer;
	private final ForeachStatement statement;
	
	public ForeachScope(ForeachStatement statement, StatementScope outer) {
		this.statement = statement;
		this.outer = outer;
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) throws CompileException {
		if (name.hasNoArguments()) {
			for (VarStatement loopVariable : statement.loopVariables) {
				if (loopVariable.name.equals(name.name))
					return new GetLocalVariableExpression(position, loopVariable);
			}
		}
		
		IPartialExpression result = super.get(position, name);
		if (result != null)
			return result;
		
		return outer.get(position, name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		return outer.getType(position, name);
	}

	@Override
	public StorageTag getStorageTag(CodePosition position, String name, String[] parameters) {
		return outer.getStorageTag(position, name, parameters);
	}

	@Override
	public LoopStatement getLoop(String name) {
		if (name == null)
			return statement;
		
		for (VarStatement loopVariable : statement.loopVariables) {
			if (loopVariable.name.equals(name))
				return statement;
		}
		
		return outer.getLoop(name);
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return outer.getFunctionHeader();
	}

	@Override
	public StoredType getThisType() {
		return outer.getThisType();
	}

	@Override
	public DollarEvaluator getDollar() {
		return outer.getDollar();
	}
	
	@Override
	public IPartialExpression getOuterInstance(CodePosition position) throws CompileException {
		return outer.getOuterInstance(position);
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return outer.getAnnotation(name);
	}

	@Override
	public TypeMemberPreparer getPreparer() {
		return outer.getPreparer();
	}

	@Override
	public GenericMapper getLocalTypeParameters() {
		return outer.getLocalTypeParameters();
	}
}
