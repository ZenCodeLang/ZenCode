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
	public static final DStyleClass EMPTY = new DStyleClass(null, NO_CLASSES);
	
	public static DStyleClass forId(String id) {
		return new DStyleClass(id, NO_CLASSES);
	}
	
	public final String id;
	public final String[] classes;
	
	public DStyleClass(String id, String[] classes) {
		this.id = id;
		this.classes = classes;
	}
}
