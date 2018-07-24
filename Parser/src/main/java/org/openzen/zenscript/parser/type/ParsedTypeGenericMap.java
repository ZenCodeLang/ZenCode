/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.definitions.ParsedTypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeGenericMap implements IParsedType {
	private final ParsedTypeParameter key;
	private final IParsedType value;
	private final int modifiers;
	
	public ParsedTypeGenericMap(ParsedTypeParameter key, IParsedType value, int modifiers) {
		this.key = key;
		this.value = value;
		this.modifiers = modifiers;
	}

	@Override
	public IParsedType withOptional() {
		return new ParsedTypeGenericMap(key, value, modifiers | TypeMembers.MODIFIER_OPTIONAL);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeGenericMap(key, value, modifiers | this.modifiers);
	}

	@Override
	public ITypeID compile(BaseScope scope) {
		TypeParameter cKey = key.compiled;
		ITypeID valueType = this.value.compile(new GenericMapScope(scope, cKey));
		
		GlobalTypeRegistry registry = scope.getTypeRegistry();
		return registry.getModified(modifiers, registry.getGenericMap(valueType, cKey));
	}
	
	private class GenericMapScope extends BaseScope {
		private final BaseScope outer;
		private final Map<String, TypeParameter> typeParameters = new HashMap<>();
		
		public GenericMapScope(BaseScope outer, TypeParameter[] parameters) {
			this.outer = outer;
			for (TypeParameter parameter : parameters)
				typeParameters.put(parameter.name, parameter);
		}
		
		public GenericMapScope(BaseScope outer, TypeParameter parameter) {
			this.outer = outer;
			typeParameters.put(parameter.name, parameter);
		}

		@Override
		public LocalMemberCache getMemberCache() {
			return outer.getMemberCache();
		}

		@Override
		public IPartialExpression get(CodePosition position, GenericName name) {
			if (typeParameters.containsKey(name.name) && name.hasNoArguments())
				return new PartialTypeExpression(position, getTypeRegistry().getGeneric(typeParameters.get(name.name)), name.arguments);
			
			return outer.get(position, name);
		}

		@Override
		public ITypeID getType(CodePosition position, List<GenericName> name) {
			if (typeParameters.containsKey(name.get(0).name) && name.size() == 1 && name.get(0).hasNoArguments())
				return getTypeRegistry().getGeneric(typeParameters.get(name.get(0).name));
			
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

		@Override
		public AnnotationDefinition getAnnotation(String name) {
			return outer.getAnnotation(name);
		}
	}
}
