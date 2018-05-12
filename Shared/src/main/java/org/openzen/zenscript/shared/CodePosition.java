/* Licensed under GPLv3 - https://opensource.org/licenses/GPL-3.0 */
package org.openzen.zenscript.shared;

import java.io.File;

/**
 * Contains the start and end positions of a token. Encompasses filename, line
 * and character range and can span multiple lines.
 * 
 * @author Stan Hebben
 */
public class CodePosition
{
	public static final CodePosition BUILTIN = new CodePosition("builtin", 0, 0, 0, 0);
	public static final CodePosition NATIVE = new CodePosition("native", 0, 0, 0, 0);
	
	public static CodePosition between(CodePosition from, CodePosition to) {
		if (!from.filename.equals(to.filename))
			throw new IllegalArgumentException("From and to positions must be in the same file!");
		
		return new CodePosition(from.filename, from.fromLine, from.fromLineOffset, to.toLine, to.toLineOffset);
	}
	
	public final String filename;
	public final int fromLine;
	public final int fromLineOffset;
	public final int toLine;
	public final int toLineOffset;
	
	public CodePosition(
			String filename,
			int fromLine, int fromLineOffset,
			int toLine, int toLineOffset)
	{
		this.filename = filename;
		this.fromLine = fromLine;
		this.fromLineOffset = fromLineOffset;
		this.toLine = toLine;
		this.toLineOffset = toLineOffset;
	}
	
	@Override
	public String toString()
	{
		if (fromLine == 0 && fromLineOffset == 0) {
			return filename;
		} else {
			return filename + ":" + fromLine + ":" + fromLineOffset;
		}
	}
	
	public String toShortString()
	{
		int lastSeparator = filename.lastIndexOf(File.separator);
		String shortFilename = lastSeparator >= 0 ? filename.substring(lastSeparator + 1) : filename;
		if (fromLine == 0 && fromLineOffset == 0)
			return shortFilename;
		
		return shortFilename + ":" + fromLine + ":" + fromLineOffset;
	}
}
