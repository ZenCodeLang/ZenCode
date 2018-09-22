/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedNamedType implements IParsedType {
	private final CodePosition position;
	private final int modifiers;
	public final List<ParsedNamePart> name;
	private final ParsedStorageTag storage;
	
	public ParsedNamedType(CodePosition position, List<ParsedNamePart> name, ParsedStorageTag storage) {
		this.position = position;
		this.modifiers = 0;
		this.name = name;
		this.storage = storage;
	}
	
	private ParsedNamedType(CodePosition position, int modifiers, List<ParsedNamePart> name, ParsedStorageTag storage) {
		this.position = position;
		this.modifiers = modifiers;
		this.name = name;
		this.storage = storage;
	}
	
	@Override
	public ITypeID compile(TypeResolutionContext context) {
		if (name.size() == 1 && name.get(0).name.equals("Iterator"))
			return toIterator(context);
		
		List<GenericName> genericNames = new ArrayList<>();
		for (ParsedNamePart namePart : name)
			genericNames.add(namePart.compile(context));
		
		StorageTag storage = this.storage.resolve(position, context);
		ITypeID baseType = context.getType(position, genericNames, storage);
		if (baseType == null)
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_TYPE, "Type not found: " + toString());
		
		ITypeID result = context.getTypeRegistry().getModified(modifiers, baseType);
		if (result == null)
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_TYPE, "Type not found: " + toString());
		
		return result;
	}
	
	@Override
	public IParsedType withOptional() {
		return new ParsedNamedType(position, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL, name, storage);
	}
	
	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedNamedType(position, this.modifiers | modifiers, name, storage);
	}
	
	@Override
	public String toString() {
		StringBuilder nameTotal = new StringBuilder();
		for (int i = 0; i < name.size(); i++) {
			if (i > 0)
				nameTotal.append(".");
			nameTotal.append(name.get(i).toString());
		}
		return nameTotal.toString();
	}
	
	@Override
	public AnnotationDefinition compileAnnotation(BaseScope scope) {
		if (name.size() != 1)
			return null;
		
		return scope.getAnnotation(name.get(0).name);
	}
	
	@Override
	public ITypeID[] compileTypeArguments(BaseScope scope) {
		ParsedNamePart last = name.get(name.size() - 1);
		return IParsedType.compileList(last.typeArguments, scope);
	}
	
	private ITypeID toIterator(TypeResolutionContext context) {
		List<IParsedType> genericTypes = name.get(0).typeArguments;
		ITypeID[] iteratorTypes = new ITypeID[genericTypes.size()];
		for (int i = 0; i < genericTypes.size(); i++)
			iteratorTypes[i] = genericTypes.get(i).compile(context);

		StorageTag storage = this.storage.resolve(position, context);
		ITypeID type = context.getTypeRegistry().getIterator(iteratorTypes, storage);
		return context.getTypeRegistry().getModified(modifiers, type);
	}
	
	public static class ParsedNamePart {
		public final String name;
		public final List<IParsedType> typeArguments;
		
		public ParsedNamePart(String name, List<IParsedType> genericArguments) {
			this.name = name;
			this.typeArguments = genericArguments;
		}
		
		private GenericName compile(TypeResolutionContext context) {
			return new GenericName(name, IParsedType.compileList(typeArguments, context));
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder(name);
			if (typeArguments != null) {
				result.append("<");
				for (int i = 0; i < typeArguments.size(); i++) {
					if (i > 0)
						result.append(", ");
					result.append(typeArguments.get(i).toString());
				}
				result.append(">");
			}
			return result.toString();
		}
	}
}
