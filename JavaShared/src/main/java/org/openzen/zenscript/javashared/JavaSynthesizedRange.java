/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingModule;

/**
 * @author Hoofdgebruiker
 */
public class JavaSynthesizedRange {
	public final JavaClass cls;
	public final JavaCompilingClass compiling;
	public final TypeParameter[] typeParameters;
	public final TypeID baseType;

	public JavaSynthesizedRange(JavaCompilingClass class_, TypeParameter[] typeParameters, TypeID baseType) {
		this.cls = class_.compiled;
		this.compiling = class_;
		this.typeParameters = typeParameters;
		this.baseType = baseType;
	}
}
