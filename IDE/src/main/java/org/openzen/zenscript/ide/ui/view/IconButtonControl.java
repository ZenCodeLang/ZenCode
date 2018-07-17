/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import java.util.function.Consumer;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DDrawableInstance;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DSimpleTooltip;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.ImmutableLiveBool;
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
public class IconButtonControl implements DComponent {
	private final DStyleClass styleClass;
	private final DDrawable icon;
	private final DDrawable iconDisabled;
	private final Consumer<DMouseEvent> onClick;
	private final LiveBool disabled;
	private final ListenerHandle<LiveBool.Listener> disabledListener;
	private final DSimpleTooltip tooltip;
	
	private DDrawSurface surface;
	private int z;
	private IconButtonControlStyle style;
	private DIRectangle bounds;
	private final MutableLiveObject<DSizing> preferences = DSizing.create();
	private boolean hover;
	private boolean press;
	
	private DShadow shadow;
	private DDrawnShape shape;
	private DDrawableInstance drawnIcon;
	
	public IconButtonControl(DStyleClass styleClass, DDrawable icon, DDrawable iconDisabled, LiveBool disabled, LiveString tooltip, Consumer<DMouseEvent> onClick) {
		this.styleClass = styleClass;
		this.icon = icon;
		this.iconDisabled = iconDisabled;
		this.onClick = onClick;
		this.disabled = disabled;
		this.tooltip = new DSimpleTooltip(DStyleClass.EMPTY, tooltip);
		disabledListener = disabled.addListener((oldValue, newValue) -> onDisabledChanged(newValue));
	}
	
	public IconButtonControl(DStyleClass styleClass, DDrawable icon, LiveString tooltip, Consumer<DMouseEvent> onClick) {
		this(styleClass, icon, icon, ImmutableLiveBool.FALSE, tooltip, onClick);
	}

	@Override
	public void mount(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		
		tooltip.setContext(surface.getContext());
		
		DStylePath path = parent.getChild("iconbutton", styleClass);
		style = new IconButtonControlStyle(surface.getStylesheet(path));
		
		int iconWidth = (int)(icon.getNominalWidth() * surface.getScale() + 0.5f);
		int iconHeight = (int)(icon.getNominalWidth() * surface.getScale() + 0.5f);
		int width = iconWidth + 2 * style.padding + 2 * style.margin;
		int height = iconHeight + 2 * style.padding + 2 * style.margin;
		preferences.setValue(new DSizing(width, height));
		
		if (bounds != null)
			setBounds(bounds);
	}
	
	@Override
	public void unmount() {
		if (shape != null)
			shape.close();
		if (drawnIcon != null)
			drawnIcon.close();
	}

	@Override
	public LiveObject<DSizing> getSizing() {
		return preferences;
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
		shape = surface.shadowPath(z, DPath.roundedRectangle(
				bounds.x + style.margin,
				bounds.y + style.margin,
				bounds.width - 2 * style.margin,
				bounds.height - 2 * style.margin,
				style.roundingRadius), DTransform2D.IDENTITY, getColor(), shadow);
		
		onDisabledChanged(disabled.getValue());
	}

	@Override
	public void close() {
		disabledListener.close();
		unmount();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		update();
		tooltip.onTargetMouseEnter(e);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		press = false;
		update();
		tooltip.onTargetMouseExit(e);
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		tooltip.onTargetMouseMove(e);
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		press = true;
		update();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		press = false;
		update();
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		onClick.accept(e);
	}
	
	private void onDisabledChanged(boolean disabled) {
		DDrawable icon = disabled ? iconDisabled : this.icon;
		
		if (drawnIcon != null)
			drawnIcon.close();
		
		drawnIcon = new DDrawableInstance(surface, z + 1, icon, DTransform2D.scaleAndTranslate(
				bounds.x + (bounds.width - icon.getNominalWidth() * surface.getScale()) / 2,
				bounds.y + (bounds.height - icon.getNominalHeight() * surface.getScale()) / 2,
				surface.getScale()));
	}
	
	private void update() {
		DShadow newShadow = getShadow();
		if (newShadow != shadow) {
			if (shape != null)
				shape.close();
			
			shadow = newShadow;
			shape = surface.shadowPath(z, DPath.roundedRectangle(
					bounds.x + style.margin,
					bounds.y + style.margin,
					bounds.width - 2 * style.margin,
					bounds.height - 2 * style.margin,
					style.roundingRadius), DTransform2D.IDENTITY, getColor(), shadow);
		} else {
			shape.setColor(getColor());
		}
	}
	
	private int getColor() {
		if (disabled.getValue())
			return style.colorDisabled;
		if (press)
			return style.colorPress;
		if (hover)
			return style.colorHover;
		
		return style.colorNormal;
	}
	
	private DShadow getShadow() {
		if (disabled.getValue())
			return style.shadowDisabled;
		if (press)
			return style.shadowPress;
		if (hover)
			return style.shadowHover;
		
		return style.shadowNormal;
	}
}
