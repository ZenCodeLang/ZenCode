package org.openzen.zenscript.compiler;

import java.util.List;

import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;

public interface CompilePlugin {
	List<ZenCodeCompiler> getCompilers();

	List<AnnotationDefinition> getAnnotations();
}
