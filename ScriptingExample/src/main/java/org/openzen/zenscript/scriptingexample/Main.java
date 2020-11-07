package org.openzen.zenscript.scriptingexample;

import org.openzen.zencode.java.*;
import org.openzen.zencode.java.logger.*;
import org.openzen.zencode.shared.*;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.lexer.*;
import org.openzen.zenscript.parser.*;
import org.openzen.zenscript.scriptingexample.gui.*;
import org.openzen.zenscript.scriptingexample.threading.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    
    public static void main(String[] args) throws CompileException, ParseException, IOException, NoSuchMethodException {
        ScriptingEngine scriptingEngine = new ScriptingEngine(new ScriptingEngineStreamLogger());
        scriptingEngine.debug = true;
        
        JavaNativeModule example = scriptingEngine.createNativeModule("example", "org.openzen.zenscript.scriptingexample");
        example.addGlobals(Globals.class);
        
        example.addClass(UpdatableGrid.class);
        example.addClass(TimeSpan.class);
        example.addClass(TimeSpan.ExpandInt.class);
        example.addClass(ZCThread.class);
        example.addClass(MyFunctionalInterfaceClass.class);
        
        scriptingEngine.registerNativeProvided(example);
        
        File inputDirectory = new File("ScriptingExample/scripts");
        final SourceFile[] sourceFiles = Files.walk(inputDirectory.getAbsoluteFile().toPath())
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(f -> f.getName().endsWith(".zs"))
                .filter(f -> !f.getAbsolutePath().contains("nope"))
                .map(f -> new FileSourceFile(f.getAbsolutePath().substring(inputDirectory.getAbsolutePath().length() + 1), f))
                .toArray(SourceFile[]::new);
        
        final PrefixedBracketParser parser = new PrefixedBracketParser(null);
        
        FunctionParameter parameter = new FunctionParameter(scriptingEngine.registry.getArray(BasicTypeID.STRING, 1), "args");
        SemanticModule scripts = scriptingEngine.createScriptedModule("script", sourceFiles, parser, new FunctionParameter[]{parameter});
        if(!scripts.isValid())
            return;
        
        scriptingEngine.registerCompiled(scripts);
        
        Map<FunctionParameter, Object> scriptArgs = new HashMap<>();
        scriptArgs.put(parameter, new String[]{"hello", "world", "example"});
        scriptingEngine.run(scriptArgs);
    }
}
