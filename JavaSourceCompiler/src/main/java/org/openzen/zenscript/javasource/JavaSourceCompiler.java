/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.compiler.ZenCodeCompiler;
import org.openzen.zenscript.javashared.JavaBaseCompiler;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionVisitor;
import org.openzen.zenscript.javashared.JavaClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceCompiler extends JavaBaseCompiler implements ZenCodeCompiler {
	public final JavaSourceFormattingSettings settings;
	public final JavaSourceSyntheticHelperGenerator helperGenerator;
	
	private final File directory;
	private final Map<File, JavaSourceFile> sourceFiles = new HashMap<>();
	public final JavaSourceContext context;
	
	public JavaSourceCompiler(File directory, CompilationUnit compilationUnit) {
		if (!directory.exists())
			directory.mkdirs();
		
		settings = new JavaSourceFormattingSettings.Builder().build();
		context = new JavaSourceContext(compilationUnit.globalTypeRegistry, directory, settings);
		helperGenerator = new JavaSourceSyntheticHelperGenerator(context, directory, settings);
		
		this.directory = directory;
	}
	
	@Override
	public void addModule(SemanticModule module) {
		context.addModule(module.module);
	}
	
	@Override
	public void addDefinition(HighLevelDefinition definition, SemanticModule module) {
		String filename = getFilename(definition);
		JavaPrepareDefinitionVisitor prepare = new JavaPrepareDefinitionVisitor(context, context.getJavaModule(module.module), filename, null);
		JavaClass cls = definition.accept(prepare);
		
		File file = new File(getDirectory(definition.pkg), cls.getName() + ".java");
		System.out.println("Compiling " + definition.name + " as " + cls.fullName);
		JavaSourceFile sourceFile = sourceFiles.get(file);
		if (sourceFile == null)
			sourceFiles.put(file, sourceFile = new JavaSourceFile(this, file, cls, module.module, definition.pkg));
		
		sourceFile.add(definition, module);
	}
	
	@Override
	public void addScriptBlock(ScriptBlock script) {
		
	}
	
	@Override
	public void finish() {
		for (JavaSourceFile sourceFile : sourceFiles.values()) {
			sourceFile.prepare(context);
		}
		
		for (JavaSourceFile sourceFile : sourceFiles.values())
			sourceFile.write();
		
		helperGenerator.write();
	}
	
	@Override
	public void run() {
		throw new UnsupportedOperationException("Cannot run this target");
	}
	
	public String getFullName(HighLevelDefinition definition) {
		return definition.pkg.fullName + "." + definition.name;
	}
	
	private File getDirectory(ZSPackage pkg) {
		if (pkg == null)
			return directory;
		
		File base = getDirectory(pkg.parent);
		return new File(base, pkg.name.replace('.', '/'));
	}
	
	private String getFilename(HighLevelDefinition definition) {
		SourceFile source = definition.getTag(SourceFile.class);
		if (source != null) {
			int slash = Math.max(source.getFilename().lastIndexOf('/'), source.getFilename().lastIndexOf('\\'));
			String filename = source.getFilename().substring(slash < 0 ? 0 : slash + 1);
			filename = filename.substring(0, filename.lastIndexOf('.'));
			return filename;
		} else {
			return definition.name == null ? "Expansion" : definition.name;
		}
	}
}
