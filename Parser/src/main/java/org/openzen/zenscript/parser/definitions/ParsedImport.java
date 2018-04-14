/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedImport {
	private final CodePosition position;
	private final List<String> name;
	private final String rename;

	public ParsedImport(CodePosition position, List<String> name, String rename) {
		this.position = position;
		this.name = name;
		this.rename = rename;
	}

	public CodePosition getPosition() {
		return position;
	}

	public List<String> getName() {
		return name;
	}

	public String getRename() {
		return rename == null ? name.get(name.size() - 1) : rename;
	}
}
