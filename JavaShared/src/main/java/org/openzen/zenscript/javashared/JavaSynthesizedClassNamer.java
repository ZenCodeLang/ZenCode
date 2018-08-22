/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSynthesizedClassNamer {
	private JavaSynthesizedClassNamer() {}
	
	public static JavaSynthesizedClass createFunctionName(FunctionTypeID function) {
		String signature = getFunctionSignature(function);
		
		String className = "Function" + signature;
		JavaClass cls = new JavaClass("zsynthetic", className, JavaClass.Kind.INTERFACE);
		return new JavaSynthesizedClass(cls, extractTypeParameters(function));
	}
	
	public static JavaSynthesizedClass createRangeName(RangeTypeID range) {
		String signature = getRangeSignature(range);
		String className = signature + "Range";
		JavaClass cls = new JavaClass("zsynthetic", className, JavaClass.Kind.CLASS);
		return new JavaSynthesizedClass(cls, extractTypeParameters(range));
	}
	
	public static TypeParameter[] extractTypeParameters(ITypeID type) {
		List<TypeParameter> result = new ArrayList<>();
		type.extractTypeParameters(result);
		return result.toArray(new TypeParameter[result.size()]);
	}
	
	public static String getFunctionSignature(FunctionTypeID type) {
		return new JavaSyntheticTypeSignatureConverter().visitFunction(type);
	}
	
	public static String getRangeSignature(RangeTypeID type) {
		return new JavaSyntheticTypeSignatureConverter().visitRange(type);
	}
}
