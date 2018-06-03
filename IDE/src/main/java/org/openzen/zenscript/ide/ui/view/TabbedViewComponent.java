/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDrawable;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewComponent {
	public final String title;
	public final DDrawable icon;
	public final DComponent content;
	
	public TabbedViewComponent(String title, DDrawable icon, DComponent content) {
		this.title = title;
		this.icon = icon;
		this.content = content;
	}
}
