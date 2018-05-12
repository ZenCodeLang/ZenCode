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
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.definitions.ParsedGenericParameter;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeGenericMap implements IParsedType {
	private final List<ParsedGenericParameter> keys;
	private final IParsedType value;
	private final int modifiers;
	
	public ParsedTypeGenericMap(List<ParsedGenericParameter> keys, IParsedType value, int modifiers) {
		this.keys = keys;
		this.value = value;
		this.modifiers = modifiers;
	}

	@Override
	public IParsedType withOptional() {
		return new ParsedTypeGenericMap(keys, value, modifiers | TypeMembers.MODIFIER_OPTIONAL);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeGenericMap(keys, value, modifiers | this.modifiers);
	}

	@Override
	public ITypeID compile(BaseScope scope) {
		TypeParameter[] cKeys = ParsedGenericParameter.getCompiled(keys);
		ITypeID valueType = this.value.compile(new GenericMapScope(scope, cKeys));
		
		GlobalTypeRegistry registry = scope.getTypeRegistry();
		return registry.getModified(modifiers, registry.getGenericMap(valueType, cKeys));
	}
	
	private class GenericMapScope extends BaseScope {
		private final BaseScope outer;
		private final Map<String, TypeParameter> typeParameters = new HashMap<>();
		
		public GenericMapScope(BaseScope outer, TypeParameter[] parameters) {
			this.outer = outer;
			for (TypeParameter parameter : parameters)
				typeParameters.put(parameter.name, parameter);
		}

		@Override
		public LocalMemberCache getMemberCache() {
			return outer.getMemberCache();
		}

		@Override
		public IPartialExpression get(CodePosition position, GenericName name) {
			if (typeParameters.containsKey(name.name) && name.arguments.isEmpty())
				return new PartialTypeExpression(position, getTypeRegistry().getGeneric(typeParameters.get(name.name)));
			
			return outer.get(position, name);
		}

		@Override
		public ITypeID getType(CodePosition position, List<GenericName> name) {
			if (typeParameters.containsKey(name.get(0).name) && name.size() == 1 && name.get(0).arguments.isEmpty())
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
	}
}
