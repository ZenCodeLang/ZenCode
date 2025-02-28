/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;

/**
 * @author Hoofdgebruiker
 */
public class FileContents {
	public final String filename;
	public final List<HighLevelDefinition> definitions = new ArrayList<>();
	public ScriptBlock script;

	public FileContents(String filename) {
		this.filename = filename;
	}
}
