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
import java.util.function.Supplier;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;

import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

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
	private final Map<String, Supplier<HighLevelDefinition>> innerTypes = new HashMap<>();
	
	public DefinitionScope(BaseScope outer, HighLevelDefinition definition) {
		this(outer, definition, true);
	}
	
	public DefinitionScope(BaseScope outer, HighLevelDefinition definition, boolean withMembers) {
		this.outer = outer;
		this.definition = definition;
		
		if (definition instanceof ExpansionDefinition) {
			ExpansionDefinition expansion = (ExpansionDefinition)definition;
			type = expansion.target;
			
			for (TypeParameter parameter : expansion.genericParameters) {
				genericParameters.put(parameter.name, parameter);
			}
		} else {
			DefinitionTypeID definitionType = outer.getTypeRegistry().getForMyDefinition(definition);
			type = definitionType;
			
			while (definitionType != null) {
				for (TypeParameter parameter : definitionType.definition.genericParameters) {
					genericParameters.put(parameter.name, parameter);
				}
				definitionType = definitionType.definition.isStatic() ? null : definitionType.outer;
			}
		}
		
		members = withMembers ? outer.getMemberCache().get(type) : null;
	}
	
	public void addInnerType(String name, Supplier<HighLevelDefinition> innerType) {
		innerTypes.put(name, innerType);
	}
	
	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		if (members != null) {
			if (members.hasInnerType(name.name))
				return new PartialTypeExpression(position, members.getInnerType(position, name), name.arguments);
			if (members.hasMember(name.name) && !name.hasArguments())
				return members.getMemberExpression(position, this, new ThisExpression(position, type), name, true);
		} else if (innerTypes.containsKey(name.name)) {
			return new PartialTypeExpression(position, getTypeRegistry().getForDefinition(innerTypes.get(name).get(), name.arguments), name.arguments);
		}
		if (genericParameters.containsKey(name.name) && !name.hasArguments())
			return new PartialTypeExpression(position, getTypeRegistry().getGeneric(genericParameters.get(name.name)), name.arguments);
		
		return outer.get(position, name);
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		if (members != null && members.hasInnerType(name.get(0).name)) {
			ITypeID result = members.getInnerType(position, name.get(0));
			for (int i = 1; i < name.size(); i++) {
				result = getTypeMembers(result).getInnerType(position, name.get(i));
			}
			return result;
		} else if (genericParameters.containsKey(name.get(0).name) && name.size() == 1 && !name.get(0).hasArguments()) {
			return getTypeRegistry().getGeneric(genericParameters.get(name.get(0).name));
		} else if (innerTypes.containsKey(name.get(0).name)) {
			ITypeID result = getTypeRegistry().getForDefinition(innerTypes.get(name.get(0).name).get(), name.get(0).arguments);
			for (int i = 1; i < name.size(); i++) {
				result = getTypeMembers(result).getInnerType(position, name.get(i)); // TODO: this cannot be right, where did the type arguments go? the abyss?
			}
			return result;
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

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return outer.getAnnotation(name);
	}

	@Override
	public TypeMemberPreparer getPreparer() {
		return outer.getPreparer();
	}
}
