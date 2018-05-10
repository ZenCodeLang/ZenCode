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
public class WhitespaceInfo {
	public static WhitespaceInfo from(String whitespaceBefore, String lineAfter, boolean skipLineBefore) {
		int numNewLines = 0;
		for (char c : whitespaceBefore.toCharArray())
			if (c == '\n')
				numNewLines++;
		
		List<String> split = StringUtils.split(whitespaceBefore, '\n');
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
	
	public boolean emptyLine;
	public String[] commentsBefore;
	public String commentsAfter;
	
	public WhitespaceInfo(boolean emptyLine, String[] commentsBefore, String commentsAfter) {
		this.emptyLine = emptyLine;
		this.commentsBefore = commentsBefore;
		this.commentsAfter = commentsAfter;
	}
}
