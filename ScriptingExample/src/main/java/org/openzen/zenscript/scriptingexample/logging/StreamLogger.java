package org.openzen.zenscript.scriptingexample.logging;

import org.openzen.zencode.java.IZSLogger;

import java.io.PrintStream;

public class StreamLogger implements IZSLogger {
    private final PrintStream infoStream, debugStream, warningStream, errorStream;

    public StreamLogger(PrintStream debugStream, PrintStream infoStream, PrintStream warningStream, PrintStream errorStream) {
        this.infoStream = infoStream;
        this.debugStream = debugStream;
        this.warningStream = warningStream;
        this.errorStream = errorStream;
    }

    public StreamLogger(PrintStream normalStream, PrintStream errorStream) {
        this(normalStream, normalStream, normalStream, errorStream);
    }

    public StreamLogger(){
        this(System.out, System.err);
    }

    @Override
    public void info(String message) {
        System.out.println("INFO: " + message);
    }

    @Override
    public void debug(String message) {
        System.out.println("DEBUG:   " + message);
    }

    @Override
    public void warning(String message) {
        System.out.println("WARNING: " + message);
    }

    @Override
    public void error(String message) {
        System.err.println("ERROR:   " + message);
    }

    @Override
    public void throwingErr(String message, Throwable throwable) {
        System.err.println("ERROR:   " + message);
        throwable.printStackTrace(System.err);
        System.err.flush();
    }

    @Override
    public void throwingWarn(String message, Throwable throwable) {
        System.err.println("WARNING: " + message);
        throwable.printStackTrace(System.out);
        System.out.flush();
    }
}
