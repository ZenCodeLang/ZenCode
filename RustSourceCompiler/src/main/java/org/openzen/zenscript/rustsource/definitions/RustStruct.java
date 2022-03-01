package org.openzen.zenscript.rustsource.definitions;

import org.openzen.zenscript.rustsource.compiler.ImportSet;

import java.util.ArrayList;
import java.util.List;

public class RustStruct extends RustDefinition {
	public final String name;
	public final boolean isPublic;
	private final List<RustField> fields = new ArrayList<>();

	public RustStruct(RustFile file, String name, boolean isPublic) {
		super(file);

		this.name = name;
		this.isPublic = isPublic;
	}

	public void addField(RustField field) {
		fields.add(field);
	}

	@Override
	public String compile(ImportSet imports) {
		StringBuilder result = new StringBuilder();
		if (isPublic)
			result.append("pub ");
		result.append("struct ");
		result.append(name);
		result.append(" {\n");
		boolean first = true;
		for (RustField field : fields) {
			if (first) {
				first = false;
			} else {
				result.append(",\n");
			}
			result.append("  ");
			if (field.isPublic)
				result.append("pub ");
			result.append(field.name);
			result.append(": ");
			result.append(field.type.compile(imports));
		}
		if (fields.size() > 0)
			result.append("\n");
		result.append("}\n");
		return result.toString();
	}
}
