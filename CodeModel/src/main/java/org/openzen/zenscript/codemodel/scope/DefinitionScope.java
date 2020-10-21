/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;

import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionScope extends BaseScope {
	private final BaseScope outer;
	private final HighLevelDefinition definition;
	private final TypeID type;
	private final TypeMembers members;
	private final TypeParameter[] typeParameters;
	private final GenericMapper typeParameterMap;
	
	public DefinitionScope(BaseScope outer, HighLevelDefinition definition) {
		this(outer, definition, true);
	}
	
	public DefinitionScope(BaseScope outer, HighLevelDefinition definition, boolean withMembers) {
		this.outer = outer;
		this.definition = definition;
		
		Map<TypeParameter, TypeID> typeParameters = new HashMap<>();
		if (definition instanceof ExpansionDefinition) {
			ExpansionDefinition expansion = (ExpansionDefinition)definition;
			type = expansion.target;
			this.typeParameters = expansion.typeParameters;
			typeParameters = TypeID.getSelfMapping(outer.getTypeRegistry(), expansion.typeParameters);
		} else {
			DefinitionTypeID definitionType = outer.getTypeRegistry().getForMyDefinition(definition);
			type = definitionType;
			
			List<TypeParameter> typeParameterList = new ArrayList<>();
			while (definitionType != null) {
				typeParameters = TypeID.getSelfMapping(outer.getTypeRegistry(), definitionType.definition.typeParameters);
				typeParameterList.addAll(Arrays.asList(definitionType.definition.typeParameters));
				
				definitionType = definitionType.definition.isStatic() ? null : definitionType.outer;
			}
			this.typeParameters = typeParameterList.toArray(new TypeParameter[typeParameterList.size()]);
		}
		
		members = withMembers ? outer.getMemberCache().get(type) : null;
		typeParameterMap = outer.getLocalTypeParameters().getInner(definition.position, getTypeRegistry(), typeParameters);
	}
	
	@Override
	public ZSPackage getRootPackage() {
		return outer.getRootPackage();
	}
	
	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) throws CompileException {
		if (members != null) {
			if (members.hasInnerType(name.name))
				return new PartialTypeExpression(position, members.getInnerType(position, name), name.arguments);
			if (members.hasMember(name.name) && !name.hasArguments())
				return members.getMemberExpression(position, this, new ThisExpression(position, type), name, true);
		}
		if (!name.hasArguments()) {
			for (TypeParameter parameter : typeParameters)
				if (parameter.name.equals(name.name))
					return new PartialTypeExpression(position, getTypeRegistry().getGeneric(parameter), name.arguments);
		}
		
		return outer.get(position, name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		if (members != null && members.hasInnerType(name.get(0).name)) {
			TypeID result = members.getInnerType(position, name.get(0));
			for (int i = 1; i < name.size(); i++) {
				result = getTypeMembers(result).getInnerType(position, name.get(i));
			}
			return result;
		} 
		
		if (name.size() == 1 && !name.get(0).hasArguments())
			for (TypeParameter parameter : typeParameters)
				if (parameter.name.equals(name.get(0).name))
					return getTypeRegistry().getGeneric(parameter);

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
	public TypeID getThisType() {
		return type;
	}

	@Override
	public DollarEvaluator getDollar() {
		return outer.getDollar();
	}
	
	@Override
	public IPartialExpression getOuterInstance(CodePosition position) throws CompileException {
		if (!definition.isInnerDefinition()) {
			throw new CompileException(position, CompileExceptionCode.NO_OUTER_BECAUSE_NOT_INNER, "Type is not an inner type; cannot access outer type");
		} else if (definition.isStatic()) {
			return new InvalidExpression(position, outer.getThisType(), CompileExceptionCode.NO_OUTER_BECAUSE_STATIC, "Inner type is static; cannot access outer type reference");
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

	@Override
	public GenericMapper getLocalTypeParameters() {
		return typeParameterMap;
	}
}
