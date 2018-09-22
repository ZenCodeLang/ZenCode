/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TypeResolutionContext {
	GlobalTypeRegistry getTypeRegistry();
	
	AnnotationDefinition getAnnotation(String name);
	
	ITypeID getType(CodePosition position, List<GenericName> name, StorageTag storage);
	
	StorageTag getStorageTag(CodePosition position, String name, String[] parameters);
	
	ITypeID getThisType();
}
