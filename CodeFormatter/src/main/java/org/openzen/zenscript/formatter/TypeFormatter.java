/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.codemodel.generic.GenericParameterBoundVisitor;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ConstTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.shared.StringUtils;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeFormatter implements ITypeVisitor<String>, GenericParameterBoundVisitor<String> {
	private final FormattingSettings settings;
	private final Importer importer;
	
	public TypeFormatter(FormattingSettings settings, Importer importer) {
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
			return element + "[" + StringUtils.times(',', array.dimension - 1) + "]";
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
		
		FunctionHeader header = function.header;
		formatTypeParameters(result, header.typeParameters);
		result.append("(");
		int parameterIndex = 0;
		for (FunctionParameter parameter : header.parameters) {
			if (parameterIndex > 0)
				result.append(", ");
			
			result.append(parameter.name);
			if (parameter.variadic)
				result.append("...");
			
			if (!settings.showAnyInFunctionHeaders || parameter.type != BasicTypeID.ANY) {
				result.append(" as ");
				result.append(header.returnType.accept(this));
			}
			
			parameterIndex++;
		}
		result.append(")");
		if (!settings.showAnyInFunctionHeaders || header.returnType != BasicTypeID.ANY) {
			result.append(" as ");
			result.append(header.returnType.accept(this));
		}
		return result.toString();
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		String importedName = importer.importDefinition(definition.definition);
		if (definition.typeParameters.length == 0)
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
		return range.from.accept(this) + " .. " + range.to.accept(this);
	}

	@Override
	public String visitConst(ConstTypeID type) {
		return "const " + type.accept(this);
	}

	@Override
	public String visitOptional(OptionalTypeID optional) {
		return optional.baseType.accept(this) + "?";
	}
	
	private void formatTypeParameters(StringBuilder result, TypeParameter[] parameters) {
		if (parameters.length > 0) {
			result.append("<");
			int index = 0;
			for (TypeParameter parameter : parameters) {
				if (index > 0)
					result.append(", ");
				
				result.append(parameter.name);
				
				if (parameter.bounds.size() > 0) {
					for (GenericParameterBound bound : parameter.bounds) {
						result.append(": ");
						result.append(bound.accept(this));
					}
				}
				
				index++;
			}
			result.append(">");
		}
	}

	@Override
	public String visitSuper(ParameterSuperBound bound) {
		return "super " + bound.type.accept(this);
	}

	@Override
	public String visitType(ParameterTypeBound bound) {
		return bound.type.accept(this);
	}
}
