package org.openzen.zencode.shared;

import java.io.IOException;
import java.io.Reader;

public interface SourceFile extends Tag {
    String getFilename();
    
    Reader open() throws IOException;
    
    void update(String content) throws IOException;
}
