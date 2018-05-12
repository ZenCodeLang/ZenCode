/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.linker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericFunctionScope extends BaseScope {
	private final BaseScope outer;
	private final Map<String, TypeParameter> parameters = new HashMap<>();
	
	public GenericFunctionScope(BaseScope outer, TypeParameter[] parameters) {
		this.outer = outer;
		
		if (parameters != null)
			for (TypeParameter parameter : parameters)
				this.parameters.put(parameter.name, parameter);
	}
	
	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		if (parameters.containsKey(name.name) && name.hasNoArguments())
			return new PartialTypeExpression(position, getTypeRegistry().getGeneric(parameters.get(name.name)));
		
		return outer.get(position, name);
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		if (name.size() == 1 && parameters.containsKey(name.get(0).name) && name.get(0).hasNoArguments())
			return getTypeRegistry().getGeneric(parameters.get(name.get(0).name));
		
		return outer.getType(position, name);
	}

	@Override
	public LoopStatement getLoop(String name) {
		return outer.getLoop(name);
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return outer.getFunctionHeader();
	}

	@Override
	public ITypeID getThisType() {
		return outer.getThisType();
	}

	@Override
	public Function<CodePosition, Expression> getDollar() {
		return outer.getDollar();
	}
	
	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		return outer.getOuterInstance(position);
	}
}
