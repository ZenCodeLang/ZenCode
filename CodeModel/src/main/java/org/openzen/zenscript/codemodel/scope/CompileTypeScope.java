/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;

/**
 *
 * @author Hoofdgebruiker
 */
public class CompileTypeScope extends BaseScope {
	private final BaseScope outer;
	private final Map<String, TypeParameter> typeParameters = new HashMap<>();

	public CompileTypeScope(BaseScope outer, TypeParameter[] parameters) {
		this.outer = outer;

		if (parameters != null)
			for (TypeParameter parameter : parameters)
				typeParameters.put(parameter.name, parameter);
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		if (typeParameters.containsKey(name.name) && !name.hasArguments())
			return new PartialTypeExpression(position, getTypeRegistry().getGeneric(typeParameters.get(name.name)), name.arguments);

		return outer.get(position, name);
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		if (typeParameters.containsKey(name.get(0).name) && name.size() == 1 && !name.get(0).hasArguments()) {
			return getTypeRegistry().getGeneric(typeParameters.get(name.get(0).name));
		}

		return outer.getType(position, name);
	}

	@Override
	public LoopStatement getLoop(String name) {
		return null;
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return null;
	}

	@Override
	public ITypeID getThisType() {
		throw new UnsupportedOperationException("Not available at this stage");
	}

	@Override
	public Function<CodePosition, Expression> getDollar() {
		return outer.getDollar();
	}

	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		return null;
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return outer.getAnnotation(name);
	}

	@Override
	public TypeMemberPreparer getPreparer() {
		return outer.getPreparer();
	}
}
