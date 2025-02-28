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

public class BuiltinCompilePlugin implements CompilePlugin {
	private final List<AnnotationDefinition> annotations;

	private BuiltinCompilePlugin() {
		annotations = Arrays.asList(
				NativeAnnotationDefinition.INSTANCE,
				PreconditionAnnotationDefinition.INSTANCE
		);
	}

	@Override
	public List<ZenCodeCompiler> getCompilers() {
		return Collections.emptyList();
	}

	@Override
	public List<AnnotationDefinition> getAnnotations() {
		return annotations;
	}
}
