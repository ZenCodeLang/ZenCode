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

import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.constructor.ModuleLoader;
import org.openzen.zenscript.constructor.module.SourceModuleReference;
import org.openzen.zenscript.constructor.module.directory.DirectorySourceModule;

/**
 * @author Hoofdgebruiker
 */
public class Main {
	public static void main(String[] args) throws IOException {
		ZSPackage root = ZSPackage.createRoot();
		ZSPackage stdlib = new ZSPackage(root, "stdlib");
		GlobalTypeRegistry registry = new GlobalTypeRegistry(stdlib);
		ModuleLoader loader = new ModuleLoader(registry, exception -> exception.printStackTrace());
		loader.register("stdlib", new SourceModuleReference(new DirectorySourceModule("stdlib", new File("../../StdLibs/stdlib"), true), true));

		SemanticModule module = loader.getModule("stdlib");
		ModuleSerializer serializer = new ModuleSerializer(new SerializationOptions(
				true,
				false,
				true,
				true,
				false));
		byte[] encoded = serializer.serialize(Collections.singletonList(module));
		System.out.println("stdlib encoded as " + encoded.length + " bytes");
		Files.write(new File("stdlib.bzm").toPath(), encoded);
	}
}
