/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.border;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.ImmutableLiveObject;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class DCustomWindowBorder implements DComponent {
	private final DStyleClass styleClass;
	private final DComponent content;
	private final LiveObject<DSizing> sizing = new ImmutableLiveObject<>(DSizing.EMPTY);
	
	private DComponentContext context;
	private DCustomWindowBorderStyle style;
	private DIRectangle bounds;
	
	private LiveBool active;
	private LiveObject<DUIWindow.State> state;
	
	private ListenerHandle<LiveObject.Listener<DUIWindow.State>> stateListener;
	private ListenerHandle<LiveBool.Listener> activeListener;
	
	private DDrawnRectangle background;
	private DDrawnShape shadowedBackground;
	private DDrawnShape border;
	
	public DCustomWindowBorder(DStyleClass styleClass, DComponent content) {
		this.styleClass = styleClass;
		this.content = content;
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("customwindowborder", styleClass);
		content.mount(context);
		
		state = parent.getUIContext().getWindow().getWindowState();
		active = parent.getUIContext().getWindow().getActive();
		stateListener = state.addListener(this::onStateChanged);
		activeListener = active.addListener(this::onActiveChanged);
		
		style = parent.getStyle(DCustomWindowBorderStyle::new);
		
		if (bounds != null)
			layout();
	}
	
	@Override
	public void unmount() {
		if (background != null)
			background.close();
		if (shadowedBackground != null)
			shadowedBackground.close();
		if (border != null)
			border.close();
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
		int contentBaseline = content.getBaselineY();
		return contentBaseline == -1 ? -1 : style.padding + style.borderWidth + contentBaseline;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		layout();
	}
	
	private void layout() {	
		if (style == null)
			return;
		
		if (state.getValue() != DUIWindow.State.MAXIMIZED) {
			DPath path = DPath.rectangle(
					bounds.x + style.padding,
					bounds.y + style.padding,
					bounds.width - 2 * style.padding - style.borderWidth,
					bounds.height - 2 * style.padding - style.borderWidth);
			if (border != null)
				border.close();
			border = context.strokePath(
					1,
					path,
					DTransform2D.IDENTITY,
					active.getValue() ? style.focusedBorderColor : style.inactiveBorderColor,
					style.borderWidth);
			
			int spacing = style.borderWidth + style.padding;
			DIRectangle inner = new DIRectangle(
					bounds.x + spacing,
					bounds.y + spacing,
					bounds.width - 2 * spacing,
					bounds.height - 2 * spacing);

			content.setBounds(inner);
			
			if (shadowedBackground != null)
				shadowedBackground.close();
			if (background != null) {
				background.close();
				background = null;
			}
			shadowedBackground = context.shadowPath(0, path, DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
		} else {
			content.setBounds(bounds);
			
			if (shadowedBackground != null) {
				shadowedBackground.close();
				shadowedBackground = null;
			}
			if (border != null) {
				border.close();
				border = null;
			}
			if (background == null) {
				background = context.fillRect(0, bounds, style.backgroundColor);
			} else {
				background.setRectangle(bounds);
			}
		}
	}

	@Override
	public void onMouseEnter(DMouseEvent e) {
		content.onMouseEnter(e);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		content.onMouseExit(e);
	}
	
	@Override
	public void onMouseMove(DMouseEvent e) {
		content.onMouseMove(e);
	}
	
	@Override
	public void onMouseDrag(DMouseEvent e) {
		content.onMouseDrag(e);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		content.onMouseClick(e);
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		content.onMouseDown(e);
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		content.onMouseRelease(e);
	}
	
	@Override
	public void onMouseScroll(DMouseEvent e) {
		content.onMouseScroll(e);
	}
	
	@Override
	public void close() {
		unmount();
	}
	
	private void onStateChanged(DUIWindow.State oldValue, DUIWindow.State newValue) {
		layout();
	}
	
	private void onActiveChanged(boolean oldValue, boolean newValue) {
		if (border != null)
			border.setColor(newValue ? style.focusedBorderColor : style.inactiveBorderColor);
	}
}
