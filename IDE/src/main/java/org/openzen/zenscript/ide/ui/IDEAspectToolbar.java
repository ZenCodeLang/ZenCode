/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.live.LiveArrayList;
import org.openzen.drawablegui.live.LiveList;

/**
 *
 * @author Hoofdgebruiker
 */
public class IDEAspectToolbar {
	public final int order;
	public final DColorableIcon icon;
	public final String title;
	public final String description;
	public final LiveList<IDEAspectBarControl> controls = new LiveArrayList<>();
	
	public IDEAspectToolbar(int order, DColorableIcon icon, String title, String description) {
		this.order = order;
		this.icon = icon;
		this.title = title;
		this.description = description;
	}
}
