/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TypeResolutionContext {
	ZSPackage getRootPackage();
	
	GlobalTypeRegistry getTypeRegistry();
	
	AnnotationDefinition getAnnotation(String name);
	
	TypeID getType(CodePosition position, List<GenericName> name);
	
	StorageTag getStorageTag(CodePosition position, String name, String[] parameters);
	
	StoredType getThisType();
}
