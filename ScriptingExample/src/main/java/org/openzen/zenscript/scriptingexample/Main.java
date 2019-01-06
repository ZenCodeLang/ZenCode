package org.openzen.zenscript.scriptingexample;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openzen.zencode.java.JavaNativeModule;
import org.openzen.zencode.java.ScriptingEngine;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.FileSourceFile;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.SimpleBracketParser;

public class Main {
	public static void main(String[] args) throws CompileException, ParseException, IOException, NoSuchMethodException {
		ScriptingEngine scriptingEngine = new ScriptingEngine();
		scriptingEngine.debug = true;
		
		JavaNativeModule example = scriptingEngine.createNativeModule("example", "org.openzen.zenscript.scriptingexample");
		example.addGlobals(Globals.class);
		example.addClass(TestClass.class);
		scriptingEngine.registerNativeProvided(example);
		
		File inputDirectory = new File("scripts");
		File[] inputFiles = Optional.ofNullable(inputDirectory.listFiles((dir, name) -> name.endsWith(".zs"))).orElseGet(() -> new File[0]);
		SourceFile[] sourceFiles = new SourceFile[inputFiles.length];
		for (int i = 0; i < inputFiles.length; i++)
			sourceFiles[i] = new FileSourceFile(inputFiles[i].getName(), inputFiles[i]);
		
		BracketExpressionParser bracketParser = new SimpleBracketParser(scriptingEngine.registry, example.loadStaticMethod(Globals.class.getMethod("bracket", String.class)));
		FunctionParameter parameter = new FunctionParameter(scriptingEngine.registry.getArray(StringTypeID.AUTO, 1).stored(), "args");
		SemanticModule scripts = scriptingEngine.createScriptedModule("script", sourceFiles, bracketParser, new FunctionParameter[] { parameter });
		if (!scripts.isValid())
			return;
		
		scriptingEngine.registerCompiled(scripts);
		
		Map<FunctionParameter, Object> scriptArgs = new HashMap<>();
		scriptArgs.put(parameter, new String[] { "hello", "world", "example" });
		scriptingEngine.run(scriptArgs);
	}
}
