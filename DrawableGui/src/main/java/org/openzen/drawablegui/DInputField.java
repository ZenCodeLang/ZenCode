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
public class DInputField implements DComponent {
	public final LiveString value;
	private final ListenerHandle<LiveString.Listener> valueListener;
	
	private final DStyleClass styleClass;
	private final LiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private DIRectangle bounds = DIRectangle.EMPTY;
	
	private DUIContext context;
	private DInputFieldStyle style;
	private DFontMetrics fontMetrics;
	private int cursorFrom = -1;
	private int cursorTo = -1;
	
	public DInputField(DStyleClass styleClass, LiveString value) {
		this.styleClass = styleClass;
		this.value = value;
		valueListener = value.addListener((oldValue, newValue) -> handleValueUpdated(newValue));
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("input", styleClass);
		style = new DInputFieldStyle(context.getStylesheets().get(context, path));
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
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		canvas.drawText(style.font, style.color, bounds.x + style.paddingLeft, bounds.y + style.paddingBottom, value.getValue());
	}

	@Override
	public void close() {
		valueListener.close();
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		context.focus(this);
	}
	
	private void handleValueUpdated(String newValue) {
		
	}
}
