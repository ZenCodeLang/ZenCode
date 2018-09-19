/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.parser.definitions.ParsedTypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeGenericMap implements IParsedType {
	private final ParsedTypeParameter key;
	private final IParsedType value;
	private final int modifiers;
	private final ParsedStorageTag storage;
	
	public ParsedTypeGenericMap(ParsedTypeParameter key, IParsedType value, int modifiers, ParsedStorageTag storage) {
		this.key = key;
		this.value = value;
		this.modifiers = modifiers;
		this.storage = storage;
	}

	@Override
	public IParsedType withOptional() {
		return new ParsedTypeGenericMap(key, value, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL, storage);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeGenericMap(key, value, modifiers | this.modifiers, storage);
	}

	@Override
	public ITypeID compile(TypeResolutionContext context) {
		TypeParameter cKey = key.compiled;
		ITypeID valueType = this.value.compile(new GenericMapScope(context, cKey));
		
		GlobalTypeRegistry registry = context.getTypeRegistry();
		return registry.getModified(modifiers, registry.getGenericMap(valueType, cKey));
	}
	
	private class GenericMapScope implements TypeResolutionContext {
		private final TypeResolutionContext outer;
		private final TypeParameter parameter;
		
		public GenericMapScope(TypeResolutionContext outer, TypeParameter parameter) {
			this.outer = outer;
			this.parameter = parameter;
		}
		
		@Override
		public GlobalTypeRegistry getTypeRegistry() {
			return outer.getTypeRegistry();
		}

		@Override
		public ITypeID getType(CodePosition position, List<GenericName> name) {
			if (name.get(0).name.equals(parameter.name) && name.size() == 1 && name.get(0).hasNoArguments())
				return getTypeRegistry().getGeneric(parameter);
			
			return outer.getType(position, name);
		}
		
		@Override
		public ITypeID getThisType() {
			return outer.getThisType();
		}

		@Override
		public AnnotationDefinition getAnnotation(String name) {
			return outer.getAnnotation(name);
		}
	}
}
