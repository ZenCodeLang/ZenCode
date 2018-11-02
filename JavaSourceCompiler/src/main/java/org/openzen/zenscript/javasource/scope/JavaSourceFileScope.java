/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.scope;

import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javasource.JavaSourceImporter;
import org.openzen.zenscript.javasource.JavaSourceObjectTypeVisitor;
import org.openzen.zenscript.javasource.JavaSourceSyntheticHelperGenerator;
import org.openzen.zenscript.javasource.JavaSourceTypeVisitor;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javasource.JavaSourceContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFileScope {
	public final JavaSourceImporter importer;
	public final JavaSourceSyntheticHelperGenerator helperGenerator;
	public final JavaClass cls;
	public final JavaSourceTypeVisitor typeVisitor;
	public final JavaSourceObjectTypeVisitor objectTypeVisitor;
	public final TypeScope semanticScope;
	public final boolean isInterface;
	public final TypeID thisType;
	public final JavaSourceContext context;
	
	public JavaSourceFileScope(
			JavaSourceImporter importer,
			JavaSourceContext context,
			JavaClass cls,
			TypeScope semanticScope,
			boolean isInterface,
			TypeID thisType)
	{
		this.importer = importer;
		this.helperGenerator = context.helperGenerator;
		this.cls = cls;
		this.semanticScope = semanticScope;
		this.isInterface = isInterface;
		this.thisType = thisType;
		this.context = context;
		
		typeVisitor = new JavaSourceTypeVisitor(importer, context);
		objectTypeVisitor = typeVisitor.objectTypeVisitor;
	}
	
	public String type(TypeID type) {
		return typeVisitor.process(type);
	}
	
	public String type(StoredType type) {
		return typeVisitor.process(type);
	}
	
	public String type(TypeID type, JavaClass rename) {
		return new JavaSourceTypeVisitor(importer, context, rename).process(type);
	}
	
	public String type(StoredType type, JavaClass rename) {
		return new JavaSourceTypeVisitor(importer, context, rename).process(type);
	}
}
