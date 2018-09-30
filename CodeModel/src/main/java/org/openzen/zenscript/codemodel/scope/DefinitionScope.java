/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
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
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StaticExpressionStorageTag;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionScope extends BaseScope {
	private final BaseScope outer;
	private final HighLevelDefinition definition;
	private final StoredType type;
	private final TypeMembers members;
	private final Map<String, TypeParameter> genericParameters = new HashMap<>();
	private final Map<String, Supplier<HighLevelDefinition>> innerTypes = new HashMap<>();
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
			type = expansion.target.stored(expansion.target.isValueType() ? ValueStorageTag.INSTANCE : BorrowStorageTag.THIS);
			
			for (TypeParameter parameter : expansion.typeParameters) {
				genericParameters.put(parameter.name, parameter);
				typeParameters.put(parameter, outer.getTypeRegistry().getGeneric(parameter));
			}
		} else {
			DefinitionTypeID definitionType = outer.getTypeRegistry().getForMyDefinition(definition);
			type = definitionType.stored(definitionType.isValueType() ? ValueStorageTag.INSTANCE : BorrowStorageTag.THIS);
			
			while (definitionType != null) {
				for (TypeParameter parameter : definitionType.definition.typeParameters) {
					genericParameters.put(parameter.name, parameter);
					typeParameters.put(parameter, outer.getTypeRegistry().getGeneric(parameter));
				}
				definitionType = definitionType.definition.isStatic() ? null : definitionType.outer;
			}
		}
		
		members = withMembers ? outer.getMemberCache().get(type) : null;
		typeParameterMap = outer.getLocalTypeParameters().getInner(getTypeRegistry(), typeParameters);
	}
	
	public void addInnerType(String name, Supplier<HighLevelDefinition> innerType) {
		innerTypes.put(name, innerType);
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
		} else if (innerTypes.containsKey(name.name)) {
			return new PartialTypeExpression(position, getTypeRegistry().getForDefinition(innerTypes.get(name).get(), name.arguments), name.arguments);
		}
		if (genericParameters.containsKey(name.name) && !name.hasArguments())
			return new PartialTypeExpression(position, getTypeRegistry().getGeneric(genericParameters.get(name.name)), name.arguments);
		
		return outer.get(position, name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		if (members != null && members.hasInnerType(name.get(0).name)) {
			TypeID result = members.getInnerType(position, name.get(0));
			for (int i = 1; i < name.size(); i++) {
				result = getTypeMembers(result.stored(StaticExpressionStorageTag.INSTANCE)).getInnerType(position, name.get(i));
			}
			return result;
		} else if (genericParameters.containsKey(name.get(0).name) && name.size() == 1 && !name.get(0).hasArguments()) {
			return getTypeRegistry().getGeneric(genericParameters.get(name.get(0).name));
		} else if (innerTypes.containsKey(name.get(0).name)) {
			TypeID result = getTypeRegistry().getForDefinition(innerTypes.get(name.get(0).name).get(), name.get(0).arguments);
			for (int i = 1; i < name.size(); i++) {
				result = getTypeMembers(result.stored(StaticExpressionStorageTag.INSTANCE)).getInnerType(position, name.get(i));
			}
			return result;
		}
		
		return outer.getType(position, name);
	}

	@Override
	public StorageTag getStorageTag(CodePosition position, String name, String[] parameters) {
		return outer.getStorageTag(position, name, parameters);
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
	public StoredType getThisType() {
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
