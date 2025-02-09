/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.ParsedFile;

/**
 * @author Hoofdgebruiker
 */
public class ParsedModule {
	public final String name;
	public final String[] dependencies;
	public final File sourceDirectory;
	public final String packageName;
	public final String javaPackageName;
	public final String host;
	//private final CompileExceptionLogger exceptionLogger;

	public ParsedModule(String name, File directory, File moduleFile, CompileExceptionLogger exceptionLogger) throws IOException {
		this.name = name;
		this.sourceDirectory = new File(directory, "src");
		//this.exceptionLogger = exceptionLogger;

		BufferedInputStream input = new BufferedInputStream(new FileInputStream(moduleFile));
		JSONObject json = new JSONObject(new JSONTokener(input));
		packageName = json.getString("package");
		javaPackageName = json.optString("javaPackageName", packageName);
		host = json.getString("host");
		JSONArray dependencies = json.optJSONArray("dependencies");
		if (dependencies == null) {
			this.dependencies = new String[0];
		} else {
			this.dependencies = new String[dependencies.length()];
			for (int i = 0; i < dependencies.length(); i++)
				this.dependencies[i] = dependencies.getString(i);
		}
	}

	public static void parse(List<ParsedFile> files, CompilingPackage pkg, BracketExpressionParser bracketParser, File directory) throws ParseException {
		for (File file : directory.listFiles()) {
			if (file.getName().endsWith(".zs")) {
				files.add(ParsedFile.parse(pkg, bracketParser, file));
			} else if (file.isDirectory()) {
				CompilingPackage innerPackage = pkg.getOrCreatePackage(file.getName());
				pkg.addPackage(file.getName(), innerPackage);
				parse(files, innerPackage, bracketParser, file);
			}
		}
	}

	public ParsedFile[] parse(CompilingPackage compilingPackage) throws ParseException {
		// TODO: load bracket parsers from host plugins
		List<ParsedFile> files = new ArrayList<>();
		parse(files, compilingPackage, null, sourceDirectory);
		return files.toArray(new ParsedFile[files.size()]);
	}
}
