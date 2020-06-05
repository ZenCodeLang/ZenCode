package org.openzen.zenscript.scriptingexample.tests.helpers;

import org.junit.jupiter.api.*;
import org.openzen.zencode.java.*;
import org.openzen.zencode.shared.*;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.lexer.*;
import org.openzen.zenscript.parser.*;
import org.openzen.zenscript.scriptingexample.tests.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

import java.util.*;


public abstract class ZenCodeTest {
    
    private final List<SourceFile> sourceFiles;
    protected ScriptingEngine engine;
    protected JavaNativeModule testModule;
    protected ZenCodeTestLogger logger;
    
    protected ZenCodeTest() {
        sourceFiles = new ArrayList<>();
    }
    
    
    @BeforeEach
    public void beforeEach() throws CompileException {
        this.logger = new ZenCodeTestLogger();
        this.engine = new ScriptingEngine(logger);
        this.testModule = engine.createNativeModule("test_module", "org.openzen.zenscript.scripting_tests");
        SharedGlobals.currentlyActiveLogger = logger;
        
        getRequiredClasses().stream().distinct().forEach(requiredClass -> {
            testModule.addGlobals(requiredClass);
            testModule.addClass(requiredClass);
        });
        engine.registerNativeProvided(testModule);
    }
    
    public void executeEngine() {
        try {
            final FunctionParameterList parameters = getParameters();
            final SemanticModule script_tests = engine.createScriptedModule("script_tests", sourceFiles
                    .toArray(new SourceFile[0]), getBEP(), parameters.getParameters());
            
            Assertions.assertTrue(script_tests.isValid(), "Scripts are not valid!");
            engine.registerCompiled(script_tests);
            engine.run(parameters.getParameterMap());
        } catch(ParseException e) {
            e.printStackTrace();
            Assertions.fail("Error in Engine execution", e);
        }
        logger.setEngineComplete();
    }
    
    public void addScript(String context) {
        sourceFiles.add(new LiteralSourceFile("test_script_" + sourceFiles.size() + ".zs", context));
    }
    
    
    public BracketExpressionParser getBEP() {
        return null;
    }
    
    public FunctionParameterList getParameters() {
        final FunctionParameterList functionParameterList = new FunctionParameterList();
        final StoredType stringArrayType = engine.registry.getArray(StringTypeID.AUTO, 1).stored();
        FunctionParameter args = new FunctionParameter(stringArrayType, "args");
        functionParameterList.addParameter(args, new String[]{"a", "b", "c"});
        return functionParameterList;
    }
    
    public List<Class<?>> getRequiredClasses() {
        final ArrayList<Class<?>> result = new ArrayList<>();
        result.add(SharedGlobals.class);
        return result;
    }
}
