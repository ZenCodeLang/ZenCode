package org.openzen.zenscript.scriptingexample.tests.helpers;

import org.junit.jupiter.api.*;
import org.openzen.zencode.java.logger.*;

import java.util.*;

public class ZenCodeTestLogger extends ScriptingEngineStreamLogger {
    
    private static final boolean logDebug = false;
    private final List<String> printlnOutputs = new ArrayList<>();
    private boolean isEngineComplete = false;
    
    @Override
    public void debug(String message) {
        if(logDebug) {
            super.debug(message);
        }
    }
    
    public void logPrintln(String line) {
        info(line);
        this.printlnOutputs.addAll(Arrays.asList(line.split(System.lineSeparator())));
    }
    
    void setEngineComplete() {
        isEngineComplete = true;
    }
    
    public void assertPrintOutput(int line, String content) {
        if(!isEngineComplete) {
            Assertions.fail("Trying to call an assertion before the engine ran, probably a fault in the test!");
        }
        Assertions.assertEquals(content, printlnOutputs.get(line));
    }
    
    public void assertPrintOutputSize(int size) {
        if(!isEngineComplete) {
            Assertions.fail("Trying to call an assertion before the engine ran, probably a fault in the test!");
        }
        Assertions.assertEquals(size, printlnOutputs.size());
    }
}
