/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import java.util.List;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.type.storage.StorageType;

/**
 *
 * @author Hoofdgebruiker
 */
public interface CompilePlugin {
	List<ZenCodeCompiler> getCompilers();
	
	List<AnnotationDefinition> getAnnotations();
	
	List<StorageType> getStorageTypes();
}
