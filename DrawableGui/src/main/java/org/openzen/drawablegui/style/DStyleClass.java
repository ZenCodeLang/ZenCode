/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.style;

/**
 *
 * @author Hoofdgebruiker
 */
public class DStyleClass {
	private static final String[] NO_CLASSES = new String[0];
	public static final DStyleClass EMPTY = new DStyleClass(null, NO_CLASSES, DEmptyStylesheet.INSTANCE);
	
	public static DStyleClass forId(String id) {
		return new DStyleClass(id, NO_CLASSES, DEmptyStylesheet.INSTANCE);
	}
	
	public static DStyleClass inline(DStylesheet stylesheet) {
		return new DStyleClass(null, NO_CLASSES, stylesheet);
	}
	
	public static DStyleClass inline(String id, DStylesheet stylesheet) {
		return new DStyleClass(id, NO_CLASSES, stylesheet);
	}
	
	public final String id;
	public final String[] classes;
	public final DStylesheet inline;
	
	public DStyleClass(String id, String[] classes, DStylesheet inline) {
		this.id = id;
		this.classes = classes;
		this.inline = inline;
	}
}
