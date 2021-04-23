package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.openzen.zenscript.codemodel.member.FieldMember;

import java.util.ArrayList;
import java.util.List;

public class JavaInitializedVariables {
	public final List<FieldMember> fields;
	public final String owner;

	public JavaInitializedVariables(String owner) {
		this.owner = owner;
		fields = new ArrayList<>();
	}

	public JavaInitializedVariables(List<FieldMember> fields, String owner) {
		this.fields = fields;
		this.owner = owner;
	}
}
