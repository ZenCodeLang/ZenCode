/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

/**
 *
 * @author Hoofdgebruiker
 */
public class CommentFormatter {
	private CommentFormatter() {}
	
	public static String[] format(String[] comments) {
		if (comments.length == 0)
			return comments;
		
		boolean isInMultilineComment = false;
		String[] result = new String[comments.length];
		for (int i = 0; i < comments.length; i++) {
			String comment = comments[i];
			if (isInMultilineComment) {
				if (!comment.startsWith("*"))
					comment = "* " + comment;
				comment = " " + comment;
			}
			
			result[i] = comment;
			
			int index = 0;
			while (true) {
				if (!isInMultilineComment && comment.indexOf("//", index) > 0)
					break;
				
				int newIndexOpen = comment.indexOf("/*", index);
				if (newIndexOpen >= 0) {
					isInMultilineComment = true;
					index = newIndexOpen;
				} else {
					break;
				}
				
				int newIndexClose = comment.indexOf("*/", newIndexOpen);
				if (newIndexClose >= 0) {
					isInMultilineComment = false;
					index = newIndexClose;
				} else {
					break;
				}
			}
		}
		
		return result;
	}
}
