/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DLineBorder;
import org.openzen.drawablegui.style.DStyleDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollPaneStyle {
	public final DBorder border;
	
	public DScrollPaneStyle(DStyleDefinition style) {
		this.border = style.getBorder("border", context -> new DLineBorder(0xFF888888, 1));
	}
}
