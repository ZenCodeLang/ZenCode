/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import java.util.function.Consumer;
import java.util.function.Function;

import listeners.ListenerHandle;
import live.LiveBool;
import live.LiveObject;
import live.MutableLiveObject;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DColorableIconInstance;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.zenscript.ide.ui.icons.ScalableCloseIcon;
import zsynthetic.FunctionBoolBoolToVoid;

/**
 *
 * @author Hoofdgebruiker
 */
public class WindowActionButton implements DComponent {
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private final Function<Float, DColorableIcon> scalableIcon;
	private final Consumer<DMouseEvent> action;
	
	private LiveBool windowFocused;
	private ListenerHandle<FunctionBoolBoolToVoid> windowFocusedListener;
	
	private DComponentContext context;
	private DColorableIcon icon;
	private DIRectangle bounds;
	private boolean hover;
	private boolean press;
	
	private DDrawnRectangle background;
	private DColorableIconInstance drawnIcon;
	
	public WindowActionButton(Function<Float, DColorableIcon> icon, Consumer<DMouseEvent> action) {
		this.scalableIcon = icon;
		this.action = action;
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("windowactionbutton", DStyleClass.EMPTY);
		
		windowFocused = context.getUIContext().getWindow().getActive();
		windowFocusedListener = windowFocused.addListener((a, b) -> update());
		
		icon = scalableIcon == null ? null : scalableIcon.apply(context.getScale());
		sizing.setValue(new DSizing(
				(int)(48 * context.getScale()),
				(int)(24 * context.getScale())));
		
		background = context.fillRect(0, DIRectangle.EMPTY, getBackgroundColor());
	}
	
	@Override
	public void unmount() {
		if (drawnIcon != null) {
			drawnIcon.close();
			drawnIcon = null;
		}
		if (background != null) {
			background.close();
			background = null;
		}
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
		background.setRectangle(bounds);
		
		if (icon != null) {
			if (drawnIcon != null)
				drawnIcon.close();
			
			int iconX = bounds.x + (int)(bounds.width - icon.getNominalWidth()) / 2;
			int iconY = bounds.y + (int)(bounds.height - icon.getNominalHeight()) / 2;
			drawnIcon = new DColorableIconInstance(context.surface, context.z + 1, icon, DTransform2D.translate(iconX, iconY), getIconColor());
		}
	}

	@Override
	public void close() {
		windowFocusedListener.close();
		unmount();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		update();
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		press = false;
		update();
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		press = true;
		update();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		if (press)
			action.accept(e);
		
		press = false;
		update();
	}
	
	private void update() {
		background.setColor(getBackgroundColor());
		if (drawnIcon != null)
			drawnIcon.setColor(getIconColor());
	}
	
	private int getBackgroundColor() {
		int color = 0xFFFFFFFF;
		
		if (hover) {
			if (icon instanceof ScalableCloseIcon) {
				color = 0xFFE81123;
			} else {
				color = 0xFFE0E0E0;
			}
		}
		if (press)
			color = 0xFFCCCCCC;
		
		return color;
	}
	
	private int getIconColor() {
		int iconColor = windowFocused.getValue() ? 0xFF000000 : 0xFF999999;
		
		if (hover) {
			if (icon instanceof ScalableCloseIcon) {
				iconColor = 0xFFFFFFFF;
			}
		}
		
		return iconColor;
	}
}
