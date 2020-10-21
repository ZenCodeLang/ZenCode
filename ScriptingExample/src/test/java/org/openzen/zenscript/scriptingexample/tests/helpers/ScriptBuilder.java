package org.openzen.zenscript.scriptingexample.tests.helpers;

import java.util.*;

public class ScriptBuilder {
    
    private final Map<String, String> scriptNameToScript;
    private StringJoiner currentScriptJoiner;
    private String currentScriptName;
    
    private ScriptBuilder() {
        scriptNameToScript = new LinkedHashMap<>();
        startNewScript();
    }
    
    /**
     * Creates a {@link StringJoiner} that merges with newlines.
     * The only real reason for this to exist is that it allows code formatting to work properly lol
     */
    public static ScriptBuilder create() {
        return new ScriptBuilder();
    }
    
    public ScriptBuilder add(String line) {
        currentScriptJoiner.add(line);
        return this;
    }
    
    public ScriptBuilder startNewScript() {
        return startNewScript(null);
    }
    
    public ScriptBuilder startNewScript(String fileName) {
        if(currentScriptJoiner != null && currentScriptJoiner.length() != 0) {
            scriptNameToScript.put(currentScriptName, currentScriptJoiner.toString());
        }
    
        currentScriptJoiner = new StringJoiner(System.lineSeparator());
        if(fileName == null) {
            currentScriptName = "test_script_" + (scriptNameToScript.size() + 1) + ".zs";
        } else {
            currentScriptName = fileName;
        }
        return this;
    }
    
    public void appendScriptsToTest(ZenCodeTest test) {
        startNewScript();
    
        scriptNameToScript.forEach((name, content) -> {
            test.addScript(content, name);
        });
    }
    
    public void execute(ZenCodeTest test, LogTolerance logTolerance) {
        appendScriptsToTest(test);
        test.executeEngine(logTolerance != LogTolerance.NO_ERRORS);
        switch(logTolerance) {
            case NO_WARNINGS:
                test.logger.assertNoWarnings(); //Fallthrough intended
            case NO_ERRORS:
                test.logger.assertNoErrors();
        }
    }
    
    public void execute(ZenCodeTest test) {
        execute(test, LogTolerance.NO_WARNINGS);
    }
    
    public enum LogTolerance {
        NO_WARNINGS, NO_ERRORS, ALLOW_ERRORS
    }
}
