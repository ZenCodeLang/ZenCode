/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

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
public class DTooltip implements DComponent {
	private final DStyleClass styleClass;
	private final LiveString tooltip;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final ListenerHandle<LiveString.Listener> tooltipListener;
	
	private DUIContext context;
	private DIRectangle bounds;
	private DFontMetrics fontMetrics;
	private DTooltipStyle style;
	private boolean visible = false;
	private DTimerHandle timerHandle = null;
	
	public DTooltip(DStyleClass styleClass, LiveString tooltip) {
		this.styleClass = styleClass;
		this.tooltip = tooltip;
		tooltipListener = tooltip.addListener(this::onTooltipChanged);
	}
	
	private void onTooltipChanged(String oldValue, String newValue) {
		if (context == null)
			return;
		
		sizing.setValue(new DSizing(
				style.border.getPaddingLeft() + fontMetrics.getWidth(newValue) + style.border.getPaddingRight(),
				style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingBottom()));
	}
	
	public void onTargetMouseEnter(DMouseEvent e) {
		if (timerHandle != null)
			timerHandle.close();
		
		timerHandle = context.setTimer(1000, this::show);
		setBounds(new DIRectangle(e.x, e.y, sizing.getValue().preferredWidth, sizing.getValue().preferredHeight));
	}
	
	public void onTargetMouseExit(DMouseEvent e) {
		if (timerHandle != null) {
			timerHandle.close();
			timerHandle = null;
		}
		
		hide();
	}
	
	private void show() {
		visible = true;
		context.repaint(bounds);
		
		if (timerHandle != null) {
			timerHandle.close();
			timerHandle = null;
		}
	}
	
	private void hide() {
		visible = false;
		context.repaint(bounds);
	}
	
	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("tooltip", styleClass);
		style = new DTooltipStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.font);
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
	}

	@Override
	public void paint(DCanvas canvas) {
		if (!visible)
			return;
		
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		style.border.paint(canvas, bounds);
		canvas.drawText(style.font, style.textColor, bounds.x + style.border.getPaddingLeft(), style.border.getPaddingTop(), tooltip.getValue());
	}

	@Override
	public void close() {
		tooltipListener.close();
	}
}
