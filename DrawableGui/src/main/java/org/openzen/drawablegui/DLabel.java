/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DLabel implements DComponent {
	private final LiveString label;
	private final DStyleClass styleClass;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final ListenerHandle<LiveString.Listener> labelListener;
	
	private DDrawSurface surface;
	private int z;
	private DIRectangle bounds;
	private DLabelStyle style;
	private DFontMetrics fontMetrics;
	
	private DDrawnText text;
	
	public DLabel(DStyleClass styleClass, LiveString label) {
		this.styleClass = styleClass;
		this.label = label;
		
		labelListener = label.addListener(this::onLabelChanged);
	}

	@Override
	public void mount(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		
		DStylePath path = parent.getChild("label", styleClass);
		style = new DLabelStyle(surface.getStylesheet(path));
		
		fontMetrics = surface.getFontMetrics(style.font);
		calculateDimension();
		
		if (text != null)
			text.close();
		text = surface.drawText(z, style.font, style.color, 0, 0, label.getValue());
	}
	
	@Override
	public void unmount() {
		if (style != null)
			style.border.close();
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
		return style.border.getPaddingTop() + fontMetrics.getAscent();
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		style.border.update(surface, z + 1, bounds);
		text.setPosition(
				bounds.x + style.border.getPaddingLeft(),
				bounds.y + style.border.getPaddingTop() + fontMetrics.getAscent());
	}

	@Override
	public void close() {
		labelListener.close();
		unmount();
	}
	
	private void onLabelChanged(String oldValue, String newValue) {
		calculateDimension();
		
		if (text != null)
			text.close();
		text = surface.drawText(
				z,
				style.font,
				style.color,
				bounds.x + style.border.getPaddingLeft(),
				bounds.y + style.border.getPaddingTop() + fontMetrics.getAscent(),
				newValue);
	}
	
	private void calculateDimension() {
		sizing.setValue(new DSizing(
			style.border.getPaddingLeft() + fontMetrics.getWidth(label.getValue()) + style.border.getPaddingRight(),
			style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingTop()));
	}
}
