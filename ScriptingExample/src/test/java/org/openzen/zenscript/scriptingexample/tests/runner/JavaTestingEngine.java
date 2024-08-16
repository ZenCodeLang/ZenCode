package org.openzen.zenscript.scriptingexample.tests.runner;

import org.openzen.zenscript.scriptingexample.tests.TestException;
import org.openzen.scriptingenginetester.TestOutput;
import org.openzen.scriptingenginetester.TestableScriptingEngine;
import org.openzen.scriptingenginetester.cases.TestCase;
import org.openzen.zencode.java.JavaNativeModuleBuilder;
import org.openzen.zencode.java.ScriptingEngine;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.scriptingexample.TestAnnotationDefinition;
import org.openzen.zenscript.scriptingexample.tests.SharedGlobals;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused") // This class is found via reflection by ScriptingEngineTester
public class JavaTestingEngine implements TestableScriptingEngine {
	public TestOutput run(TestCase test) {
		EngineTestLogger logger = new EngineTestLogger();
		ScriptingEngine engine = new ScriptingEngine(
				logger,
				ScriptingEngine.class::getResourceAsStream,
				test.getRequiredStdLibModules().toArray(new String[0])
		);
		engine.debug = true;
		engine.addAnnotation(TestAnnotationDefinition.INSTANCE);

		JavaNativeModuleBuilder testModule = engine.createNativeModule("testsupport", "org.openzen.zenscript.scripting_tests");
		SharedGlobals.currentlyActiveLogger = logger;

		getRequiredClasses().stream().distinct().forEach(requiredClass -> {
			testModule.addGlobals(requiredClass);
			testModule.addClass(requiredClass);
		});
		testModule.addGlobal("typeof", TypeofGlobal.INSTANCE);
		testModule.addGlobal("testAnnotationValue", TestAnnotationValueGlobal.INSTANCE);

		try {
			testModule.complete();
		} catch (CompileException ex) {
			logger.logCompileException(ex);
		}
		run(engine, logger, test.getSourceFiles());
		return logger.getOutput();
	}

	private void run(ScriptingEngine engine, EngineTestLogger logger, List<SourceFile> sourceFiles) {
		try {
			final SemanticModule scriptTests = engine.createScriptedModule("script_tests", sourceFiles
					.toArray(new SourceFile[0]), null, FunctionParameter.NONE, "testsupport");
			final boolean scriptsValid = scriptTests.isValid();
			if (scriptsValid) {
				engine.registerCompiled(scriptTests);
				engine.run();
			}
		} catch (ParseException e) {
			logger.logCompileException(new CompileException(e.position, CompileErrors.parseError(e.message)));
		}
	}

	public List<Class<?>> getRequiredClasses() {
		final ArrayList<Class<?>> result = new ArrayList<>();
		result.add(SharedGlobals.class);
		result.add(TestException.class);
		return result;
	}
}
