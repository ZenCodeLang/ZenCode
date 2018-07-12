/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DSimpleTooltip {
	private final DStyleClass styleClass;
	private final LiveString tooltip;
	private final ListenerHandle<LiveString.Listener> tooltipListener;
	
	private DUIContext context;
	private DIRectangle bounds;
	private DFontMetrics fontMetrics;
	private DTooltipStyle style;
	private boolean visible = false;
	private DTimerHandle timerHandle = null;
	
	public DSimpleTooltip(DStyleClass styleClass, LiveString tooltip) {
		this.styleClass = styleClass;
		this.tooltip = tooltip;
		tooltipListener = tooltip.addListener(this::onTooltipChanged);
	}
	
	private void onTooltipChanged(String oldValue, String newValue) {
		if (context == null || bounds == null)
			return;
		
		bounds = new DIRectangle(
				bounds.x,
				bounds.y,
				style.border.getPaddingLeft() + fontMetrics.getWidth(tooltip.getValue()) + style.border.getPaddingRight(),
				style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingBottom());
		context.repaint(bounds);
	}
	
	public void onTargetMouseEnter(DMouseEvent e) {
		if (timerHandle != null)
			timerHandle.close();
		
		timerHandle = context.setTimer(1000, this::show);
		bounds = new DIRectangle(
				e.x,
				e.y,
				style.border.getPaddingLeft() + fontMetrics.getWidth(tooltip.getValue()) + style.border.getPaddingRight(),
				style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingBottom());
	}
	
	public void onTargetMouseExit(DMouseEvent e) {
		if (timerHandle != null) {
			timerHandle.close();
			timerHandle = null;
		}
		
		hide();
	}
	
	private void show() {
		System.out.println("Show tooltip");
		visible = true;
		context.repaint(bounds);
		
		if (timerHandle != null) {
			timerHandle.close();
			timerHandle = null;
		}
	}
	
	private void hide() {
		System.out.println("Hide tooltip");
		visible = false;
		context.repaint(bounds);
	}
	
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("tooltip", styleClass);
		style = new DTooltipStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.font);
	}
	
	public void paint(DCanvas canvas) {
		if (!visible)
			return;
		
		System.out.println("Actually paint tooltip");
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		style.border.paint(canvas, bounds);
		canvas.drawText(style.font, style.textColor, bounds.x + style.border.getPaddingLeft(), bounds.y + style.border.getPaddingTop(), tooltip.getValue());
	}
	
	public void close() {
		tooltipListener.close();
	}
}
