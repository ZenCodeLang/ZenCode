/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DLabel implements DComponent {
	private final LiveString label;
	private final DStyleClass styleClass;
	private final LiveObject<DDimensionPreferences> preferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private final ListenerHandle<LiveString.Listener> labelListener;
	
	private DUIContext context;
	private DIRectangle bounds;
	private DLabelStyle style;
	private DFontMetrics fontMetrics;
	
	public DLabel(DStyleClass styleClass, LiveString label) {
		this.styleClass = styleClass;
		this.label = label;
		
		labelListener = label.addListener(this::onLabelChanged);
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("label", styleClass);
		style = new DLabelStyle(context.getStylesheets().get(context, path));
		
		fontMetrics = context.getFontMetrics(style.font);
		calculateDimension();
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return preferences;
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
	}

	@Override
	public void paint(DCanvas canvas) {
		style.border.paint(canvas, bounds);
		canvas.drawText(style.font, style.color, bounds.x + style.border.getPaddingLeft(), bounds.y + style.border.getPaddingTop() + fontMetrics.getAscent(), label.getValue());
	}

	@Override
	public void close() {
		labelListener.close();
	}
	
	private void onLabelChanged(String oldValue, String newValue) {
		calculateDimension();
		context.repaint(bounds);
	}
	
	private void calculateDimension() {
		preferences.setValue(new DDimensionPreferences(
			style.border.getPaddingLeft() + fontMetrics.getWidth(label.getValue()) + style.border.getPaddingRight(),
			style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingTop()));
	}
}
