package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.statement.Statement;

import java.util.List;

public class ScriptBlock extends Taggable {
	public final SourceFile file;
	public final Module module;
	public final ZSPackage pkg;
	public final FunctionHeader scriptHeader;
	public final List<Statement> statements;

	public ScriptBlock(SourceFile file, Module module, ZSPackage pkg, FunctionHeader scriptHeader, List<Statement> statements) {
		this.file = file;
		this.module = module;
		this.pkg = pkg;
		this.scriptHeader = scriptHeader;
		this.statements = statements;
	}

	public ScriptBlock withStatements(List<Statement> newStatements) {
		ScriptBlock result = new ScriptBlock(file, module, pkg, scriptHeader, newStatements);
		result.addAllTagsFrom(this);
		return result;
	}
}
