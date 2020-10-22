/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeAnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.PreconditionAnnotationDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class BuiltinCompilePlugin implements CompilePlugin {
	public static final BuiltinCompilePlugin BUILTIN = new BuiltinCompilePlugin();
	
	private final List<AnnotationDefinition> annotations;
	private final List<StorageType> storageTypes;
	
	private BuiltinCompilePlugin() {
		annotations = Arrays.asList(
				NativeAnnotationDefinition.INSTANCE,
				PreconditionAnnotationDefinition.INSTANCE
		);
		
		storageTypes = Arrays.asList(StorageType.getStandard());
	}

	@Override
	public List<ZenCodeCompiler> getCompilers() {
		return Collections.emptyList();
	}

	@Override
	public List<AnnotationDefinition> getAnnotations() {
		return annotations;
	}

	@Override
	public List<StorageType> getStorageTypes() {
		return storageTypes;
	}
}
