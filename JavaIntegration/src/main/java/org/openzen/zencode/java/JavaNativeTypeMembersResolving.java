package org.openzen.zencode.java;

import org.openzen.zencode.java.module.JavaNativeTypeMembers;
import org.openzen.zencode.java.module.JavaNativeTypeTemplate;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public class JavaNativeTypeMembersResolving implements ResolvingType {
	private final JavaNativeTypeTemplate template;
	private final TypeID type;
	private final GenericMapper mapper;

	public JavaNativeTypeMembersResolving(JavaNativeTypeTemplate template, TypeID type, GenericMapper mapper) {
		this.template = template;
		this.type = type;
		this.mapper = mapper;
	}

	@Override
	public TypeID getType() {
		return type;
	}

	@Override
	public ResolvedType withExpansions(List<ExpansionSymbol> expansions) {
		return new JavaNativeTypeMembers(template, type, mapper);
	}
}
