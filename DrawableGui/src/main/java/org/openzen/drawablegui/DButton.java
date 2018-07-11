/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DButton implements DComponent {
	private final DStyleClass styleClass;
	private final LiveString label;
	private final LiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private final LiveBool disabled;
	
	private DUIContext context;
	private DIRectangle bounds;
	
	private DButtonStyle style;
	private DFontMetrics fontMetrics;
	
	private boolean hovering = false;
	private boolean pressing = false;
	
	public DButton(DStyleClass styleClass, LiveString label, LiveBool disabled) {
		this.styleClass = styleClass;
		this.label = label;
		this.disabled = disabled;
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("Button", styleClass);
		this.style = new DButtonStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.font);
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public int getBaselineY() {
		return style.paddingTop + fontMetrics.getAscent();
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		int backgroundColor = style.backgroundColorNormal;
		if (hovering)
			backgroundColor = style.backgroundColorHover;
		if (pressing)
			backgroundColor = style.backgroundColorPress;
		if (disabled.getValue())
			backgroundColor = style.backgroundColorDisabled;
		
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor);
	}

	@Override
	public void close() {
		
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hovering = true;
		repaint();
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hovering = false;
		pressing = false;
		repaint();
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		pressing = true;
		repaint();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		pressing = false;
		
		if (!disabled.getValue()) {
			// TODO
		}
		
		repaint();
	}
	
	private void repaint() {
		if (context == null || bounds == null)
			return;
		
		context.repaint(bounds);
	}
}
