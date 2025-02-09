package org.openzen.zenscript.rustsource;

import org.openzen.zenscript.rustsource.compiler.ImportSet;
import org.openzen.zenscript.rustsource.definitions.RustConst;
import org.openzen.zenscript.rustsource.definitions.RustDefinition;
import org.openzen.zenscript.rustsource.definitions.RustFile;

import java.util.ArrayList;
import java.util.List;

public class RustSourceFile {
	public final RustFile file;
	public final ImportSet imports = new ImportSet();

	private final List<String> consts = new ArrayList<>();
	private final List<String> definitions = new ArrayList<>();

	public RustSourceFile(RustFile file) {
		this.file = file;
	}

	public void addConst(String rsConst) {
		consts.add(rsConst);
	}

	public void addDefinition(String definition) {
		definitions.add(definition);
	}

	public String compile() {
		StringBuilder result = new StringBuilder();
		imports.compileTo(result);

		if (consts.size() > 0) {
			result.append("\n");
			for (String const_ : consts) {
				result.append(const_).append('\n');
			}
		}

		for (String compiled : definitions) {
			result.append('\n');
			result.append(compiled);
		}
		return result.toString();
	}
}
