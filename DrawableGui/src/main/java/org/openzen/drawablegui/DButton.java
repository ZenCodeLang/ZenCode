/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DDrawnText;
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
	
	private DDrawSurface surface;
	private int z;
	private DIRectangle bounds;
	
	private DButtonStyle style;
	private DFontMetrics fontMetrics;
	
	private boolean hovering = false;
	private boolean pressing = false;
	
	private DDrawnShape shape;
	private DDrawnText text;
	private DShadow currentShadow;
	
	public DButton(DStyleClass styleClass, LiveString label, LiveBool disabled, Runnable action) {
		this.styleClass = styleClass;
		this.label = label;
		this.disabled = disabled;
		this.action = action;
	}
	
	@Override
	public void mount(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		
		DStylePath path = parent.getChild("Button", styleClass);
		this.style = new DButtonStyle(surface.getStylesheet(path));
		fontMetrics = surface.getFontMetrics(style.font);
		
		sizing.setValue(new DSizing(
				style.paddingLeft + style.paddingRight + fontMetrics.getWidth(label.getValue()),
				style.paddingTop + style.paddingBottom + fontMetrics.getAscent() + fontMetrics.getDescent()));
		currentShadow = getShadow();
	}
	
	@Override
	public void unmount() {
		surface = null;
		
		if (shape != null)
			shape.close();
		if (text != null)
			text.close();
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
		
		if (shape != null)
			shape.close();
		if (text != null)
			text.close();
		
		shape = surface.shadowPath(
				z,
				DPath.roundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 2 * surface.getScale()),
				DTransform2D.IDENTITY,
				getBackgroundColor(),
				currentShadow);
		text = surface.drawText(
				z +  1, 
				style.font,
				style.textColor,
				bounds.x + style.paddingLeft,
				bounds.y + style.paddingTop + fontMetrics.getAscent(),
				label.getValue());
	}

	@Override
	public void close() {
		unmount();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hovering = true;
		update();
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hovering = false;
		pressing = false;
		update();
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		pressing = true;
		update();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		pressing = false;
		
		if (!disabled.getValue()) {
			action.run();
		}
		
		update();
	}
	
	private void update() {
		DShadow newShadow = getShadow();
		if (newShadow != currentShadow) {
			currentShadow = newShadow;
			
			if (shape != null)
				shape.close();
			
			shape = surface.shadowPath(
				z,
				DPath.roundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 2 * surface.getScale()),
				DTransform2D.IDENTITY,
				getBackgroundColor(),
				currentShadow);
		} else {
			shape.setColor(getBackgroundColor());
		}
	}
	
	private DShadow getShadow() {
		if (disabled.getValue())
			return style.shadowDisabled;
		if (pressing)
			return style.shadowPress;
		if (hovering)
			return style.shadowHover;
		
		return style.shadowNormal;
	}
	
	private int getBackgroundColor() {
		if (disabled.getValue())
			return style.backgroundColorDisabled;
		if (pressing)
			return style.backgroundColorPress;
		if (hovering)
			return style.backgroundColorHover;
		
		return style.backgroundColorNormal;
	}
}
