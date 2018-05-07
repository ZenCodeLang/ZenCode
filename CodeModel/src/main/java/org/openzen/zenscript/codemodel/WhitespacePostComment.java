/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.shared.StringUtils;

/**
 *
 * @author Hoofdgebruiker
 */
public class WhitespacePostComment {
	public static WhitespacePostComment fromWhitespace(String whitespace) {
		List<String> comments = new ArrayList<>();
		for (String line : StringUtils.split(whitespace, '\n')) {
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
