/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import live.LiveBool;
import live.LiveString;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDrawable;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewComponent {
	public final LiveString title;
	public final DDrawable icon;
	public final DComponent content;
	public final LiveBool updated;
	
	public TabbedViewComponent(LiveString title, DDrawable icon, DComponent content, LiveBool updated) {
		this.title = title;
		this.icon = icon;
		this.content = content;
		this.updated = updated;
	}
}
