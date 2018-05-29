/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.scroll;

import org.openzen.drawablegui.border.DBorder;
import org.openzen.drawablegui.border.DLineBorder;

/**
 *
 * @author Hoofdgebruiker
 */
public class DScrollPaneStyle {
	public static final DScrollPaneStyle DEFAULT = new DScrollPaneStyle(new DLineBorder(0xFF888888, 1), DScrollBarStyle.DEFAULT);
	
	public final DBorder border;
	public final DScrollBarStyle scrollBarStyle;
	
	public DScrollPaneStyle(
			DBorder border,
			DScrollBarStyle scrollBarStyle) {
		this.border = border;
		this.scrollBarStyle = scrollBarStyle;
	}
}
