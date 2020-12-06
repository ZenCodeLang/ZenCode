package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.Tag;
import stdlib.Strings;

import java.util.ArrayList;
import java.util.List;

public class WhitespaceInfo implements Tag {
	public boolean emptyLine;
	public String[] commentsBefore;
	public String commentsAfter;
	public WhitespaceInfo(boolean emptyLine, String[] commentsBefore, String commentsAfter) {
		this.emptyLine = emptyLine;
		this.commentsBefore = commentsBefore;
		this.commentsAfter = commentsAfter;
	}

	public static WhitespaceInfo from(String whitespaceBefore, String lineAfter, boolean skipLineBefore) {
		int numNewLines = 0;
		for (char c : whitespaceBefore.toCharArray())
			if (c == '\n')
				numNewLines++;

		String[] split = Strings.split(whitespaceBefore, '\n');
		List<String> commentsBefore = new ArrayList<>();
		for (String splitLine : split) {
			String trimmed = splitLine.trim();
			if (trimmed.isEmpty())
				continue;

			commentsBefore.add(trimmed);
		}

		boolean emptyLine = !skipLineBefore && numNewLines - commentsBefore.size() > 0;
		return new WhitespaceInfo(
				emptyLine,
				commentsBefore.toArray(new String[commentsBefore.size()]),
				lineAfter.trim());
	}
}
