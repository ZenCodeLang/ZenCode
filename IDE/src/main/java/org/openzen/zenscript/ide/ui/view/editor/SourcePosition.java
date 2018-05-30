/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourcePosition {
	public final TokenModel tokens;
	public final int line;
	public final int offset;
	
	private long positionVersion;
	private TokenModel.Position position;

	public SourcePosition(TokenModel tokens, int line, int offset) {
		this.tokens = tokens;
		this.line = line;
		this.offset = offset;
	}
	
	public static SourcePosition min(SourcePosition a, SourcePosition b) {
		if (a.line < b.line)
			return a;
		if (a.line > b.line)
			return b;
		if (a.offset < b.offset)
			return a;
		if (a.offset > b.offset)
			return b;

		return a;
	}
	
	public static SourcePosition max(SourcePosition a, SourcePosition b) {
		if (a.line < b.line)
			return b;
		if (a.line > b.line)
			return a;
		if (a.offset < b.offset)
			return b;
		if (a.offset > b.offset)
			return a;

		return b;
	}

	public boolean equals(SourcePosition other) {
		return line == other.line && offset == other.offset;
	}
	
	public TokenModel.Position asTokenPosition() {
		if (position == null || positionVersion != tokens.getVersion()) {
			positionVersion = tokens.getVersion();
			position = tokens.getPosition(line, offset);
		}
		
		return position;
	}
}
