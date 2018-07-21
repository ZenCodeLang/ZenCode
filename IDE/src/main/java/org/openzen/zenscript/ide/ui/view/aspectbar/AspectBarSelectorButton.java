/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import java.util.function.Consumer;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DDrawableInstance;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DSimpleTooltip;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.ImmutableLiveString;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DShadow;
import org.openzen.drawablegui.style.DStyleClass;

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
	
	private DComponentContext context;
	private AspectBarSelectorButtonStyle style;
	private DIRectangle bounds = DIRectangle.EMPTY;
	private DDrawnShape shape;
	private DShadow shadow;
	private DDrawableInstance iconInstance;
	
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
		
		activeListener = active.addListener((oldValue, newValue) -> update());
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("selectorbutton", styleClass);
		style = context.getStyle(AspectBarSelectorButtonStyle::new);
		sizing.setValue(new DSizing(style.width, style.height));
		
		tooltip.setContext(context.getUIContext());
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
		
		if (shape != null)
			shape.close();
		
		shadow = getShadow();
		shape = context.shadowPath(
				0,
				DPath.roundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.roundingRadius),
				DTransform2D.IDENTITY,
				getColor(),
				shadow);
		
		if (iconInstance != null)
			iconInstance.close();
		iconInstance = new DDrawableInstance(context.surface, context.z + 1, icon, DTransform2D.scaleAndTranslate(
				bounds.x + (style.width - icon.getNominalWidth() * context.getScale()) / 2,
				bounds.y + (style.height - icon.getNominalHeight() * context.getScale()) / 2,
				context.getScale()));
	}
	
	@Override
	public void unmount() {
		if (shape != null)
			shape.close();
		if (iconInstance != null)
			iconInstance.close();
		
		tooltip.close();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hovering = true;
		tooltip.onTargetMouseEnter(e);
		update();
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
		update();
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		pressing = true;
		update();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		if (pressing)
			onClick.accept(e);
		
		pressing = false;
		update();
	}

	@Override
	public void close() {
		activeListener.close();
		tooltip.close();
		
		unmount();
	}
	
	private void update() {
		if (context == null)
			return;
		
		DShadow newShadow = getShadow();
		if (newShadow != shadow) {
			shadow = newShadow;
			
			if (shape != null)
				shape.close();
			
			shape = context.shadowPath(
				0,
				DPath.roundedRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.roundingRadius),
				DTransform2D.IDENTITY,
				getColor(),
				shadow);
		} else {
			shape.setColor(getColor());
		}
	}
	
	private DShadow getShadow() {
		if (active.getValue())
			return style.shadowActive;
		if (pressing)
			return style.shadowPress;
		if (hovering)
			return style.shadowHover;
		
		return style.shadowNormal;
	}
	
	private int getColor() {
		if (active.getValue())
			return style.colorActive;
		if (pressing)
			return style.colorPress;
		if (hovering)
			return style.colorHover;
		
		return style.colorNormal;
	}
}
