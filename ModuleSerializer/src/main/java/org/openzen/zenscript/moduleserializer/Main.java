/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.module.DirectoryModuleReference;

/**
 *
 * @author Hoofdgebruiker
 */
public class Main {
	public static void main(String[] args) throws IOException {
		CompilationUnit compilationUnit = new CompilationUnit();
		ModuleLoader loader = new ModuleLoader(compilationUnit, exception -> exception.printStackTrace());
		loader.register("stdlib", new DirectoryModuleReference("stdlib", new File("../../StdLibs/stdlib"), true));
		
		SemanticModule module = loader.getModule("stdlib");
		ModuleSerializer serializer = new ModuleSerializer(new SerializationOptions(
				true,
				false,
				false,
				true,
				false));
		byte[] encoded = serializer.serialize(Collections.singletonList(module));
		System.out.println("stdlib encoded as " + encoded.length + " bytes");
		Files.write(new File("stdlib.bzm").toPath(), encoded);
	}
}
