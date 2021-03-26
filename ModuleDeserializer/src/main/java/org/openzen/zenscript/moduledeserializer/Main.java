/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduledeserializer;

import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.serialization.DeserializationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeAnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.PreconditionAnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.compiler.ModuleRegistry;
import org.openzen.zenscript.constructor.module.logging.EmptyModuleLogger;

/**
 * @author Hoofdgebruiker
 */
public class Main {
	public static void main(String[] args) throws IOException, DeserializationException {
		byte[] data = Files.readAllBytes(new File("../ModuleSerializer/stdlib.bzm").toPath());

		ZSPackage rootPackage = ZSPackage.createRoot();
		AnnotationDefinition[] annotations = new AnnotationDefinition[]{
				NativeAnnotationDefinition.INSTANCE,
				PreconditionAnnotationDefinition.INSTANCE
		};
		final GlobalTypeRegistry globalTypeRegistry = new GlobalTypeRegistry(rootPackage);
		final ModuleRegistry modules = new ModuleRegistry();
		final IZSLogger logger = new EmptyModuleLogger();

		ModuleDeserializer deserializer = new ModuleDeserializer(modules, globalTypeRegistry, annotations, rootPackage, logger);
		SemanticModule[] libs = deserializer.deserialize(data);

	}
}
