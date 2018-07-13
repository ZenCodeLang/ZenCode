/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import java.util.function.Consumer;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DSimpleTooltip;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.ImmutableLiveBool;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveObject;
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
	
	private DUIContext context;
	private IconButtonControlStyle style;
	private DIRectangle bounds;
	private final MutableLiveObject<DSizing> preferences = DSizing.create();
	private boolean hover;
	private boolean press;
	private DPath shape;
	
	public IconButtonControl(DStyleClass styleClass, DDrawable icon, DDrawable iconDisabled, LiveBool disabled, LiveString tooltip, Consumer<DMouseEvent> onClick) {
		this.styleClass = styleClass;
		this.icon = icon;
		this.iconDisabled = iconDisabled;
		this.onClick = onClick;
		this.disabled = disabled;
		this.tooltip = new DSimpleTooltip(DStyleClass.EMPTY, tooltip);
		disabledListener = disabled.addListener((oldValue, newValue) -> repaint());
	}
	
	public IconButtonControl(DStyleClass styleClass, DDrawable icon, LiveString tooltip, Consumer<DMouseEvent> onClick) {
		this(styleClass, icon, icon, ImmutableLiveBool.FALSE, tooltip, onClick);
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		tooltip.setContext(context);
		
		DStylePath path = parent.getChild("iconbutton", styleClass);
		style = new IconButtonControlStyle(context.getStylesheets().get(context, path));
		
		int iconWidth = (int)(icon.getNominalWidth() * context.getScale() + 0.5f);
		int iconHeight = (int)(icon.getNominalWidth() * context.getScale() + 0.5f);
		int width = iconWidth + 2 * style.padding + 2 * style.margin;
		int height = iconHeight + 2 * style.padding + 2 * style.margin;
		preferences.setValue(new DSizing(width, height));
		
		if (bounds != null)
			setBounds(bounds);
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
		
		if (context != null)
			shape = DPath.roundedRectangle(
					bounds.x + style.margin,
					bounds.y + style.margin,
					bounds.width - 2 * style.margin,
					bounds.height - 2 * style.margin,
					style.roundingRadius);
	}

	@Override
	public void paint(DCanvas canvas) {
		if (disabled.getValue()) {
			canvas.shadowPath(shape, DTransform2D.IDENTITY, style.shadowDisabled);
			canvas.fillPath(shape, DTransform2D.IDENTITY, style.colorDisabled);
		} else if (press) {
			canvas.shadowPath(shape, DTransform2D.IDENTITY, style.shadowPress);
			canvas.fillPath(shape, DTransform2D.IDENTITY, style.colorPress);
		} else if (hover) {
			canvas.shadowPath(shape, DTransform2D.IDENTITY, style.shadowHover);
			canvas.fillPath(shape, DTransform2D.IDENTITY, style.colorHover);
		} else {
			canvas.shadowPath(shape, DTransform2D.IDENTITY, style.shadowNormal);
			canvas.fillPath(shape, DTransform2D.IDENTITY, style.colorNormal);
		}
		
		DDrawable icon = disabled.getValue() ? iconDisabled : this.icon;
		icon.draw(canvas, DTransform2D.scaleAndTranslate(
				bounds.x + (bounds.width - icon.getNominalWidth() * context.getScale()) / 2,
				bounds.y + (bounds.height - icon.getNominalHeight() * context.getScale()) / 2,
				context.getScale()));
	}

	@Override
	public void close() {
		disabledListener.close();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		repaint();
		tooltip.onTargetMouseEnter(e);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		press = false;
		repaint();
		tooltip.onTargetMouseExit(e);
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		tooltip.onTargetMouseMove(e);
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		press = true;
		repaint();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		press = false;
		repaint();
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		onClick.accept(e);
	}
	
	private void repaint() {
		if (context == null || bounds == null)
			return;
		
		context.repaint(bounds);
	}
}
