/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import java.util.function.Consumer;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.listeners.DIRectangle;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class AspectBarSelectorButton implements DComponent {
	
	public final LiveBool active;
	
	private final DStyleClass styleClass;
	private final DDrawable icon;
	private final LiveObject<DDimensionPreferences> preferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private final Consumer<DMouseEvent> onClick;
	private DUIContext context;
	private AspectBarSelectorButtonStyle style;
	private DIRectangle bounds;
	private DPath shape;
	private boolean hovering;
	private boolean pressing;
	
	private final ListenerHandle<LiveBool.Listener> activeListener;
	
	public AspectBarSelectorButton(DStyleClass styleClass, DDrawable icon, LiveBool active, Consumer<DMouseEvent> onClick) {
		this.active = active;
		this.styleClass = styleClass;
		this.icon = icon;
		this.onClick = onClick;
		
		activeListener = active.addListener((oldValue, newValue) -> repaint());
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		DStylePath path = parent.getChild("selectorbutton", styleClass);
		style = new AspectBarSelectorButtonStyle(context.getStylesheets().get(context, path));
		preferences.setValue(new DDimensionPreferences(style.width, style.height));
		shape = DPath.roundedRectangle(
				0,
				0,
				style.width,
				style.height,
				style.roundingRadius);
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
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		int color = style.colorNormal;
		DShadow shadow = style.shadowNormal;
		if (active.getValue()) {
			color = style.colorActive;
			shadow = style.shadowActive;
		} else if (pressing) {
			color = style.colorPress;
			shadow = style.shadowPress;
		} else if (hovering) {
			color = style.colorHover;
			shadow = style.shadowHover;
		}
		
		canvas.shadowPath(
					shape,
					DTransform2D.translate(bounds.x, bounds.y),
					shadow);
		canvas.fillPath(
				shape,
				DTransform2D.translate(bounds.x, bounds.y),
				color);
		icon.draw(canvas, DTransform2D.scaleAndTranslate(
				bounds.x + (style.width - icon.getNominalWidth() * context.getScale()) / 2,
				bounds.y + (style.height - icon.getNominalHeight() * context.getScale()) / 2,
				context.getScale()));
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
		if (pressing)
			onClick.accept(e);
		
		pressing = false;
		repaint();
	}

	@Override
	public void close() {
		// nothing
	}
	
	private void repaint() {
		if (context != null)
			context.repaint(bounds);
	}
}
