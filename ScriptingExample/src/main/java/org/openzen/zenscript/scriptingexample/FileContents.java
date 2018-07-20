/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;

/**
 *
 * @author Hoofdgebruiker
 */
public class FileContents {
	public final SourceFile file;
	public ScriptBlock script;
	public final List<HighLevelDefinition> definitions = new ArrayList<>();
	
	public FileContents(SourceFile file) {
		this.file = file;
	}
}
