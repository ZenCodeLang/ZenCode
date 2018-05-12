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

import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionScope extends BaseScope {
	private final BaseScope outer;
	private final HighLevelDefinition definition;
	private final ITypeID type;
	private final TypeMembers members;
	private final Map<String, TypeParameter> genericParameters = new HashMap<>();
	
	public DefinitionScope(BaseScope outer, HighLevelDefinition definition) {
		this.outer = outer;
		this.definition = definition;
		
		ITypeID[] genericParameterList = null;
		if (definition.genericParameters != null) {
			genericParameterList = new ITypeID[definition.genericParameters.length];
			for (int i = 0; i < definition.genericParameters.length; i++) {
				TypeParameter parameter = definition.genericParameters[i];
				genericParameterList[i] = outer.getTypeRegistry().getGeneric(parameter);
				genericParameters.put(parameter.name, parameter);
			}
		}
		
		if (definition instanceof ExpansionDefinition) {
			ExpansionDefinition expansion = (ExpansionDefinition)definition;
			type = expansion.target;
		} else {
			type = outer.getTypeRegistry().getForDefinition(definition, genericParameterList);
		}
		members = outer.getMemberCache().get(type);
	}
	
	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		if (members.hasInnerType(name.name))
			return new PartialTypeExpression(position, members.getInnerType(position, name));
		if (members.hasMember(name.name) && !name.hasArguments())
			return members.getMemberExpression(position, new ThisExpression(position, type), name, true);
		if (genericParameters.containsKey(name.name) && !name.hasArguments())
			return new PartialTypeExpression(position, getTypeRegistry().getGeneric(genericParameters.get(name.name)));
		
		return outer.get(position, name);
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		if (members.hasInnerType(name.get(0).name)) {
			ITypeID result = members.getInnerType(position, name.get(0));
			for (int i = 1; i < name.size(); i++) {
				result = getTypeMembers(result).getInnerType(position, name.get(i));
			}
			return result;
		} else if (genericParameters.containsKey(name.get(0).name) && name.size() == 1 && !name.get(0).hasArguments()) {
			return getTypeRegistry().getGeneric(genericParameters.get(name.get(0).name));
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
		return type;
	}

	@Override
	public Function<CodePosition, Expression> getDollar() {
		return outer.getDollar();
	}
	
	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		if (!definition.isInnerDefinition()) {
			throw new CompileException(position, CompileExceptionCode.NO_OUTER_BECAUSE_NOT_INNER, "Type is not an inner type; cannot access outer type");
		} else if (definition.isStatic()) {
			throw new CompileException(position, CompileExceptionCode.NO_OUTER_BECAUSE_STATIC, "Inner type is static; cannot access outer type reference");
		} else {
			// TODO
			throw new UnsupportedOperationException("not yet supported");
		}
	}
}
