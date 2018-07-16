/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import java.util.function.Consumer;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DSimpleTooltip;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.ImmutableLiveString;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
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
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final Consumer<DMouseEvent> onClick;
	private DDrawSurface surface;
	private AspectBarSelectorButtonStyle style;
	private DIRectangle bounds = DIRectangle.EMPTY;
	private DPath shape;
	private boolean hovering;
	private boolean pressing;
	private final DSimpleTooltip tooltip;
	
	private final ListenerHandle<LiveBool.Listener> activeListener;
	
	public AspectBarSelectorButton(DStyleClass styleClass, DDrawable icon, LiveBool active, String tooltip, Consumer<DMouseEvent> onClick) {
		this.active = active;
		this.styleClass = styleClass;
		this.icon = icon;
		this.onClick = onClick;
		this.tooltip = new DSimpleTooltip(DStyleClass.EMPTY, new ImmutableLiveString(tooltip));
		
		activeListener = active.addListener((oldValue, newValue) -> repaint());
	}

	@Override
	public void setSurface(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		DStylePath path = parent.getChild("selectorbutton", styleClass);
		style = new AspectBarSelectorButtonStyle(surface.getStylesheet(path));
		sizing.setValue(new DSizing(style.width, style.height));
		shape = DPath.roundedRectangle(
				0,
				0,
				style.width,
				style.height,
				style.roundingRadius);
		
		tooltip.setContext(surface.getContext());
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
		return -1;
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
					color,
					shadow);
		icon.draw(canvas, DTransform2D.scaleAndTranslate(
				bounds.x + (style.width - icon.getNominalWidth() * surface.getScale()) / 2,
				bounds.y + (style.height - icon.getNominalHeight() * surface.getScale()) / 2,
				surface.getScale()));
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hovering = true;
		tooltip.onTargetMouseEnter(e);
		repaint();
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		tooltip.onTargetMouseMove(e);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hovering = false;
		pressing = false;
		tooltip.onTargetMouseExit(e);
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
		activeListener.close();
		tooltip.close();
	}
	
	private void repaint() {
		if (surface != null && bounds != null)
			surface.repaint(bounds);
	}
}
