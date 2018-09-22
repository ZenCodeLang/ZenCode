/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduledeserializer;

import org.openzen.zenscript.codemodel.serialization.DeserializationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeAnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.PreconditionAnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.storage.AnyStorageType;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageType;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageType;
import org.openzen.zenscript.codemodel.type.storage.StaticStorageType;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageType;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.compiler.ModuleRegistry;
import org.openzen.zenscript.compiler.SemanticModule;

/**
 *
 * @author Hoofdgebruiker
 */
public class Main {
	public static void main(String[] args) throws IOException, DeserializationException {
		byte[] data = Files.readAllBytes(new File("../ModuleSerializer/stdlib.bzm").toPath());
		
		ZSPackage rootPackage = ZSPackage.createRoot();
		AnnotationDefinition[] annotations = new AnnotationDefinition[] {
			NativeAnnotationDefinition.INSTANCE,
			PreconditionAnnotationDefinition.INSTANCE
		};
		StorageType[] storageTypes = StorageType.getStandard();
		CompilationUnit compilationUnit = new CompilationUnit();
		ModuleRegistry modules = new ModuleRegistry();
		ModuleDeserializer deserializer = new ModuleDeserializer(modules, compilationUnit, annotations, storageTypes, rootPackage);
		SemanticModule[] libs = deserializer.deserialize(data);
		
	}
}
