package org.openzen.zencode.shared.logging;

public interface IZSLogger {
    
    void info(String message);
    
    void debug(String message);
    
    void trace(String message);
    
    void warning(String message);
    
    void error(String message);
    
    void throwingErr(String message, Throwable throwable);
    
    void throwingWarn(String message, Throwable throwable);
}
