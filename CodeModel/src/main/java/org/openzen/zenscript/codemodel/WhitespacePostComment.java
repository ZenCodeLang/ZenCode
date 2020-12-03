package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.Tag;
import stdlib.Strings;

public class WhitespacePostComment implements Tag {
	public static WhitespacePostComment fromWhitespace(String whitespace) {
		List<String> comments = new ArrayList<>();
		for (String line : Strings.split(whitespace, '\n')) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			
			comments.add(line);
		}
		
		if (comments.isEmpty())
			return null;
		
		return new WhitespacePostComment(comments.toArray(new String[comments.size()]));
	}
	
	public final String[] comments;
	
	public WhitespacePostComment(String[] comments) {
		this.comments = comments;
	}
}
