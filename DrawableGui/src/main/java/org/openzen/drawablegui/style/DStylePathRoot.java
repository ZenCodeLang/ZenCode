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
public class DStylePathRoot implements DStylePath {
	public static final DStylePathRoot INSTANCE = new DStylePathRoot();
	
	private DStylePathRoot() {}

	@Override
	public DStylePath getChild(String element, DStyleClass styleClass) {
		return new DStyleChildElement(this, element, styleClass);
	}
	
	private static class DStyleChildElement implements DStylePath {
		private final DStylePath parent;
		private final String element;
		private final DStyleClass styleClass;
		
		public DStyleChildElement(DStylePath parent, String element, DStyleClass styleClass) {
			this.parent = parent;
			this.element = element;
			this.styleClass = styleClass;
		}

		@Override
		public DStylePath getChild(String element, DStyleClass styleClass) {
			return new DStyleChildElement(this, element, styleClass);
		}
	}
}
