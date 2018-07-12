/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DButton implements DComponent {
	private final DStyleClass styleClass;
	private final LiveString label;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final LiveBool disabled;
	private final Runnable action;
	
	private DUIContext context;
	private DIRectangle bounds;
	
	private DButtonStyle style;
	private DFontMetrics fontMetrics;
	
	private boolean hovering = false;
	private boolean pressing = false;
	
	public DButton(DStyleClass styleClass, LiveString label, LiveBool disabled, Runnable action) {
		this.styleClass = styleClass;
		this.label = label;
		this.disabled = disabled;
		this.action = action;
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("Button", styleClass);
		this.style = new DButtonStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.font);
		
		sizing.setValue(new DSizing(
				style.paddingLeft + style.paddingRight + fontMetrics.getWidth(label.getValue()),
				style.paddingTop + style.paddingBottom + fontMetrics.getAscent() + fontMetrics.getDescent()));
	}

	@Override
	public LiveObject<DSizing> getSizing() {
		return sizing;
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
		DShadow shadow = style.shadowNormal;
		if (hovering) {
			backgroundColor = style.backgroundColorHover;
			shadow = style.shadowNormal;
		}
		if (pressing) {
			backgroundColor = style.backgroundColorPress;
			shadow = style.shadowPress;
		}
		if (disabled.getValue()) {
			backgroundColor = style.backgroundColorDisabled;
			shadow = style.shadowDisabled;
		}
		
		DPath shape = DPath.roundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 2 * context.getScale());
		canvas.shadowPath(shape, DTransform2D.IDENTITY, shadow);
		canvas.fillPath(shape, DTransform2D.IDENTITY, backgroundColor);
		canvas.drawText(style.font, style.textColor, bounds.x + style.paddingLeft, bounds.y + style.paddingTop + fontMetrics.getAscent(), label.getValue());
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
			action.run();
		}
		
		repaint();
	}
	
	private void repaint() {
		if (context == null || bounds == null)
			return;
		
		context.repaint(bounds);
	}
}
