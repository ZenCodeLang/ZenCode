/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class FileFormatter {
	private final FormattingSettings settings;
	
	public FileFormatter(FormattingSettings settings) {
		this.settings = settings;
	}
	
	// Formats the given scripts and definitions into a file
	public String format(ZSPackage pkg, ScriptBlock script, List<HighLevelDefinition> definitions) {
		FileImporter importer = new FileImporter(pkg);
		
		TypeFormatter typeFormatter = new TypeFormatter(settings, importer);
		ExpressionFormatter expressionFormatter = new ExpressionFormatter(settings, typeFormatter);
		
		StatementFormatter scriptFormatter = new StatementFormatter("", settings, expressionFormatter);
		for (Statement statement : script.statements) {
			statement.accept(scriptFormatter);
		}
		
		StringBuilder output = new StringBuilder();
		importer.write(output);
		output.append(scriptFormatter.toString().trim());
		return output.toString();
	}
	
	private class FileImporter implements Importer {
		private final ZSPackage pkg;
		private final Map<String, HighLevelDefinition> imports = new HashMap<>();
		
		public FileImporter(ZSPackage pkg) {
			this.pkg = pkg;
		}
		
		public void write(StringBuilder output) {
			Map<ZSPackage, List<HighLevelDefinition>> importsByPackage = new HashMap<>();
			for (HighLevelDefinition definition : imports.values()) {
				if (!importsByPackage.containsKey(definition.pkg))
					importsByPackage.put(definition.pkg, new ArrayList<>());
				
				importsByPackage.get(definition.pkg).add(definition);
			}
			
			List<ZSPackage> sortedPackages = new ArrayList<>(importsByPackage.keySet());
			Collections.sort(sortedPackages, (a, b) -> a.fullName.compareTo(b.fullName));
			for (ZSPackage pkg : sortedPackages) {
				for (HighLevelDefinition definition : importsByPackage.get(pkg))
					output.append("import ").append(pkg.fullName).append('.').append(definition.name).append(";\n");
			}
			
			if (imports.size() > 0)
				output.append("\n");
		}
		
		@Override
		public String importDefinition(HighLevelDefinition definition) {
			if (definition.pkg == pkg)
				return definition.name;
			
			if (imports.get(definition.name) == definition)
				return definition.name;
			
			if (imports.containsKey(definition.name) || pkg.contains(definition.name)) {
				return definition.pkg.fullName + '.' + definition.name;
			} else {
				imports.put(definition.name, definition);
				return definition.name;
			}
		}
	}
}
