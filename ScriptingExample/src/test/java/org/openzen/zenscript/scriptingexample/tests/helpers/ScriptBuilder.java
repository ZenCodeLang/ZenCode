package org.openzen.zenscript.scriptingexample.tests.helpers;

import java.util.*;

public class ScriptBuilder {
    
    private final List<String> scripts;
    private StringJoiner currentScriptJoiner;
    
    private ScriptBuilder() {
        scripts = new ArrayList<>();
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
        if(currentScriptJoiner != null) {
            scripts.add(currentScriptJoiner.toString());
        }
        
        currentScriptJoiner = new StringJoiner(System.lineSeparator());
        return this;
    }
    
    public void appendScriptsToTest(ZenCodeTest test) {
        startNewScript();
    
        for(String script : scripts) {
            test.addScript(script);
        }
    }
    
    public void execute(ZenCodeTest test, LogTolerance logTolerance) {
        appendScriptsToTest(test);
        test.executeEngine();
        switch(logTolerance) {
            case NO_WARNINGS:
                test.logger.assertNoWarnings();
            case NO_ERRORS:
                test.logger.assertNoErrors();
                break;
            case ALLOW_ERRORS:
                break;
        }
    }
    
    public void execute(ZenCodeTest test) {
        execute(test, LogTolerance.NO_WARNINGS);
    }
    
    public enum LogTolerance {
        NO_WARNINGS, NO_ERRORS, ALLOW_ERRORS
    }
}
