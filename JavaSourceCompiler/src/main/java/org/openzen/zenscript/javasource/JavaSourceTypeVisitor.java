/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.javashared.JavaSynthesizedClass;
import org.openzen.zenscript.codemodel.generic.GenericParameterBoundVisitor;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.javashared.JavaClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceTypeVisitor implements ITypeVisitor<String>, GenericParameterBoundVisitor<String> {
	public final JavaSourceImporter importer;
	public final JavaSourceSyntheticTypeGenerator typeGenerator;
	public final JavaSourceObjectTypeVisitor objectTypeVisitor;
	public final JavaClass cls;
	
	public JavaSourceTypeVisitor(JavaSourceImporter importer, JavaSourceSyntheticTypeGenerator typeGenerator) {
		this(importer, typeGenerator, null);
	}
	
	public JavaSourceTypeVisitor(JavaSourceImporter importer, JavaSourceSyntheticTypeGenerator typeGenerator, JavaClass cls) {
		this.importer = importer;
		this.typeGenerator = typeGenerator;
		this.cls = cls;
		
		if (this instanceof JavaSourceObjectTypeVisitor) {
			objectTypeVisitor = (JavaSourceObjectTypeVisitor)this;
		} else {
			objectTypeVisitor = new JavaSourceObjectTypeVisitor(importer, typeGenerator);
		}
	}

	@Override
	public String visitBasic(BasicTypeID basic) {
		switch (basic) {
			case VOID: return "void";
			case BOOL: return "boolean";
			case BYTE: return "int";
			case SBYTE: return "byte";
			case SHORT: return "short";
			case USHORT: return "int";
			case INT: return "int";
			case UINT: return "int";
			case LONG: return "long";
			case ULONG: return "long";
			case FLOAT: return "float";
			case DOUBLE: return "double";
			case CHAR: return "char";
			case STRING: return "String";
			default:
				throw new IllegalArgumentException("Unknown basic type: " + basic);
		}
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		StringBuilder result = new StringBuilder();
		result.append(array.elementType.accept(this));
		for (int i = 0; i < array.dimension; i++)
			result.append("[]");
		
		return result.toString();
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		String map = importer.importType(JavaClass.MAP);
		return map + "<" + assoc.keyType.accept(new JavaSourceObjectTypeVisitor(importer, typeGenerator)) + ", " + assoc.valueType.accept(new JavaSourceObjectTypeVisitor(importer, typeGenerator)) + ">";
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		return importer.importType(JavaClass.MAP) + "<Class<?>, Object>";
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		if (iterator.iteratorTypes.length == 1) {
			return importer.importType(JavaClass.ITERATOR) + "<" + iterator.iteratorTypes[0].accept(objectTypeVisitor) + '>';
		} else {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
		JavaSynthesizedClass synthetic = typeGenerator.synthesizeFunction(function);
		StringBuilder result = new StringBuilder();
		result.append(importer.importType(synthetic.cls));
		if (synthetic.typeParameters.length > 0) {
			result.append('<');
			for (int i = 0; i < synthetic.typeParameters.length; i++) {
				if (i > 0)
					result.append(", ");
				
				result.append(synthetic.typeParameters[i].name);
			}
			result.append('>');
		}
		return result.toString();
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		StringBuilder result = new StringBuilder();
		format(result, definition, cls, false);
		return result.toString();
	}
	
	private void format(StringBuilder output, DefinitionTypeID type, JavaClass cls, boolean isStatic) {
		if (type.outer != null) {
			format(output, type.outer, null, type.definition.isStatic() || type.definition.isInterface());
			output.append(".");
			output.append(type.definition.name);
		} else {
			output.append(cls == null ? importer.importType(type.definition) : importer.importType(cls));
		}
		
		if (!isStatic && type.typeParameters.length > 0) {
			output.append("<");
			for (int i = 0; i < type.typeParameters.length; i++) {
				if (i > 0)
					output.append(", ");
				output.append(type.typeParameters[i].accept(objectTypeVisitor));
			}
			output.append(">");
		}
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		return generic.parameter.name;
	}

	@Override
	public String visitRange(RangeTypeID range) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String visitModified(ModifiedTypeID optional) {
		return optional.baseType.accept(new JavaSourceObjectTypeVisitor(importer, typeGenerator));
	}

	@Override
	public String visitSuper(ParameterSuperBound bound) {
		return " super " + bound.type.accept(this);
	}

	@Override
	public String visitType(ParameterTypeBound bound) {
		return " extends " + bound.type.accept(this);
	}
}
