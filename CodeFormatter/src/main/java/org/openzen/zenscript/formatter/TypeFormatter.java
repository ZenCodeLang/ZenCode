/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import org.openzen.zenscript.formattershared.Importer;
import org.openzen.zenscript.codemodel.generic.GenericParameterBoundVisitor;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import stdlib.Chars;
import org.openzen.zenscript.codemodel.type.TypeVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeFormatter implements TypeVisitor<String>, GenericParameterBoundVisitor<String> {
	private final ScriptFormattingSettings settings;
	private final Importer importer;
	
	public TypeFormatter(ScriptFormattingSettings settings, Importer importer) {
		this.settings = settings;
		this.importer = importer;
	}

	@Override
	public String visitBasic(BasicTypeID basic) {
		return basic.name;
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		String element = array.elementType.accept(this);
		if (array.dimension == 1) {
			return element + "[]";
		} else {
			return element + "[" + Chars.times(',', array.dimension - 1) + "]";
		}
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		return assoc.valueType.accept(this) + "[" + assoc.keyType.accept(this) + "]";
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
		StringBuilder result = new StringBuilder();
		result.append("function");
		FormattingUtils.formatHeader(result, settings, function.header, this);
		return result.toString();
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		String importedName = importer.importDefinition(definition.definition);
		if (definition.typeParameters == null)
			return importedName;
		
		StringBuilder result = new StringBuilder();
		result.append(importedName);
		result.append("<");
		int index = 0;
		for (ITypeID typeParameter : definition.typeParameters) {
			if (index > 0)
				result.append(", ");
			
			result.append(typeParameter.accept(this));
		}
		result.append(">");
		return result.toString();
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		return generic.parameter.name;
	}

	@Override
	public String visitRange(RangeTypeID range) {
		return range.baseType.accept(this) + " .. " + range.baseType.accept(this);
	}

	@Override
	public String visitModified(ModifiedTypeID type) {
		StringBuilder result = new StringBuilder();
		if (type.isConst())
			result.append("const ");
		if (type.isImmutable())
			result.append("immutable ");
		result.append(type.accept(this));
		if (type.isOptional())
			result.append("?");
		
		return result.toString();
	}

	@Override
	public String visitSuper(ParameterSuperBound bound) {
		return "super " + bound.type.accept(this);
	}

	@Override
	public String visitType(ParameterTypeBound bound) {
		return bound.type.accept(this);
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		StringBuilder result = new StringBuilder();
		result.append(map.value.accept(this));
		result.append("[<");
		FormattingUtils.formatTypeParameters(result, new TypeParameter[] { map.key }, this);
		result.append("]>");
		return result.toString();
	}
}
