/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.scope;

import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javasource.JavaSourceImporter;
import org.openzen.zenscript.javasource.JavaSourceObjectTypeVisitor;
import org.openzen.zenscript.javasource.JavaSourceSyntheticHelperGenerator;
import org.openzen.zenscript.javasource.JavaSourceSyntheticTypeGenerator;
import org.openzen.zenscript.javasource.JavaSourceTypeVisitor;
import org.openzen.zenscript.javasource.tags.JavaSourceClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFileScope {
	public final JavaSourceImporter importer;
	public final JavaSourceSyntheticTypeGenerator typeGenerator;
	public final JavaSourceSyntheticHelperGenerator helperGenerator;
	public final JavaSourceClass cls;
	public final JavaSourceTypeVisitor typeVisitor;
	public final JavaSourceObjectTypeVisitor objectTypeVisitor;
	public final TypeScope semanticScope;
	public final boolean isInterface;
	public final ITypeID thisType;
	
	public JavaSourceFileScope(
			JavaSourceImporter importer, 
			JavaSourceSyntheticTypeGenerator typeGenerator,
			JavaSourceSyntheticHelperGenerator helperGenerator,
			JavaSourceClass cls,
			TypeScope semanticScope,
			boolean isInterface,
			ITypeID thisType)
	{
		this.importer = importer;
		this.typeGenerator = typeGenerator;
		this.helperGenerator = helperGenerator;
		this.cls = cls;
		this.semanticScope = semanticScope;
		this.isInterface = isInterface;
		this.thisType = thisType;
		
		typeVisitor = new JavaSourceTypeVisitor(importer, typeGenerator);
		objectTypeVisitor = typeVisitor.objectTypeVisitor;
	}
	
	public String type(ITypeID type) {
		return type.accept(typeVisitor);
	}
	
	public String type(ITypeID type, JavaSourceClass rename) {
		return type.accept(new JavaSourceTypeVisitor(importer, typeGenerator, rename));
	}
}
