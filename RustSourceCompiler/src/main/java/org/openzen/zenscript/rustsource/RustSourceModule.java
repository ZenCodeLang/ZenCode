package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.rustsource.definitions.*;

import java.util.*;

public class RustSourceModule {
	public final Module module;
	public final FunctionParameter[] scriptParameters;

	private final Map<HighLevelDefinition, RustStruct> structs = new HashMap<>();
	private final Map<HighLevelDefinition, RustStruct> expansionClasses = new HashMap<>();
	private final Map<HighLevelDefinition, RustNativeType> nativeTypes = new HashMap<>();
	private final Map<ImplementationMember, RustImplementation> implementations = new HashMap<>();
	private final Map<IDefinitionMember, RustField> fields = new HashMap<>();
	private final Map<IDefinitionMember, RustMethod> methods = new HashMap<>();
	private final Map<VariantDefinition.Option, RustVariantOption> variantOptions = new HashMap<>();

	public final List<SourceFile> sourceFiles = new ArrayList<>();

	public RustSourceModule(Module module, FunctionParameter[] parameters) {
		this.module = module;
		this.scriptParameters = parameters;
	}

	public static class SourceFile {
		public final String filename;
		public final String content;

		public SourceFile(String filename, String content) {
			this.filename = filename;
			this.content = content;
		}
	}
}
