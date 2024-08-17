package org.openzen.zencode.shared;

import java.util.List;

public final class CodePosition {
	public static final CodePosition BUILTIN = new CodePosition(new VirtualSourceFile("builtin"), 0, 0, 0, 0);
	public static final CodePosition NATIVE = new CodePosition(new VirtualSourceFile("native"), 0, 0, 0, 0);
	public static final CodePosition META = new CodePosition(new VirtualSourceFile("meta"), 0, 0, 0, 0);
	public static final CodePosition UNKNOWN = new CodePosition(new VirtualSourceFile("unknown"), 0, 0, 0, 0);
	public static final CodePosition GENERATED = new CodePosition(new VirtualSourceFile("generated"), 0, 0, 0, 0);
	public final SourceFile file;
	public final int fromLine;
	public final int fromLineOffset;
	public final int toLine;
	public final int toLineOffset;

	public CodePosition(SourceFile file, int fromLine, int fromLineOffset, int toLine, int toLineOffset) {
		this.file = file;
		this.fromLine = fromLine;
		this.fromLineOffset = fromLineOffset;
		this.toLine = toLine;
		this.toLineOffset = toLineOffset;
	}

	public String getFilename() {
		return file.getFilename();
	}

	public String toShortString() {
		List<String> filePath = file.getFilePath();
		String shortFilename = filePath.get(filePath.size() - 1);
		if (fromLine == 0 && fromLineOffset == 0)
			return shortFilename;
		return shortFilename + ":" + fromLine + ":" + fromLineOffset;
	}

	public CodePosition until(CodePosition to) {
		if (!(file == to.file))
			throw new AssertionError("From and to positions must be in the same file!");
		return new CodePosition(file, fromLine, fromLineOffset, to.toLine, to.toLineOffset);
	}

	public CodePosition merge(CodePosition to) {
		if (!(file == to.file))
			throw new AssertionError("From and to positions must be in the same file!");


		CodePosition earlier;
		if(this.fromLine == to.fromLine) {
			earlier = this.fromLineOffset <= to.fromLineOffset ? this : to;
		} else {
			earlier = this.fromLine < to.fromLine ? this : to;
		}

		CodePosition later = earlier == this ? to : this;

		return new CodePosition(file, earlier.fromLine, earlier.fromLineOffset, later.toLine, later.toLineOffset);
	}

	public CodePosition withLength(int characters) {
		return new CodePosition(file, fromLine, fromLineOffset, fromLine, fromLineOffset + characters);
	}

	public String toString() {
		return fromLine == 0 && fromLineOffset == 0 ? file.getFilename() : file.getFilename() + ":" + Integer.toString(fromLine) + ":" + Integer.toString(fromLineOffset);
	}

	public SourceFile getFile() {
		return file;
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
