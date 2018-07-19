package org.openzen.zencode.shared;

import org.openzen.zencode.shared.CodePosition;

public final class CodePosition {
	public static final CodePosition BUILTIN = new CodePosition(new VirtualSourceFile("builtin"), 0, 0, 0, 0);
	public static final CodePosition NATIVE = new CodePosition(new VirtualSourceFile("native"), 0, 0, 0, 0);
	public final String filename;
	public final SourceFile file;
	public final int fromLine;
	public final int fromLineOffset;
	public final int toLine;
	public final int toLineOffset;
	
	public CodePosition(SourceFile file, int fromLine, int fromLineOffset, int toLine, int toLineOffset) {
		this.file = file;
	    this.filename = file.getFilename();
	    this.fromLine = fromLine;
	    this.fromLineOffset = fromLineOffset;
	    this.toLine = toLine;
	    this.toLineOffset = toLineOffset;
	}
	
	public String toShortString() {
	    int lastSeparator = filename.lastIndexOf('/');
	    String shortFilename = lastSeparator >= 0 ? filename.substring(lastSeparator + 1, (filename).length()) : filename;
	    if (fromLine == 0 && fromLineOffset == 0)
	        return shortFilename;
	    return shortFilename + ":" + Integer.toString(fromLine) + ":" + Integer.toString(fromLineOffset);
	}
	
	public CodePosition until(CodePosition to) {
	    if (!filename.equals(to.filename))
	        throw new AssertionError("From and to positions must be in the same file!");
	    return new CodePosition(file, fromLine, fromLineOffset, to.toLine, to.toLineOffset);
	}
	
	public String toString() {
	    return fromLine == 0 && fromLineOffset == 0 ? filename : filename + ":" + Integer.toString(fromLine) + ":" + Integer.toString(fromLineOffset);
	}
	
	public String getFilename() {
	    return filename;
	}
	
	public int getFromLine() {
	    return fromLine;
	}
	
	public int getFromLineOffset() {
	    return fromLineOffset;
	}
	
	public int getToLine() {
	    return toLine;
	}
	
	public int getToLineOffset() {
	    return toLineOffset;
	}
}
