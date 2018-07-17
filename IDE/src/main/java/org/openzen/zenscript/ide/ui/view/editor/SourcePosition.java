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
		if (line < 0)
			throw new IllegalArgumentException("line cannot be negative");
		if (offset < 0)
			throw new IllegalArgumentException("offset cannot be negative");
		
		this.tokens = tokens;
		this.line = line;
		this.offset = offset;
	}
	
	public SourcePosition advance(int characters) {
		if (characters <= 0) {
			throw new IllegalArgumentException("Characters must be >= 0");
		} else if (characters == 0) {
			return this;
		} else {
			int line = this.line;
			int offset = this.offset;
			while (offset + characters > tokens.getLineLength(line) && line < tokens.getLineCount() - 1) {
				characters -= tokens.getLineLength(line) - offset + 1; // make sure to include the newline
				offset = 0;
				line++;
			}
			if (line >= tokens.getLineCount() -1)
				return new SourcePosition(tokens, tokens.getLineCount() - 1, tokens.getLineLength(tokens.getLineCount() - 1));
			
			return new SourcePosition(tokens, line, offset + characters);
		}
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
