/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import live.LiveString;

import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class DSimpleTooltip {
	private final DStyleClass styleClass;
	private final LiveString tooltip;
	
	private DUIContext context;
	private DSimpleTooltipStyle style;
	private DTimerHandle timerHandle = null;
	
	private int enterMouseX;
	private int enterMouseY;
	private DUIWindow window;
	
	public DSimpleTooltip(DStyleClass styleClass, LiveString tooltip) {
		this.styleClass = styleClass;
		this.tooltip = tooltip;
	}
	
	public void setContext(DUIContext context) {
		this.context = context;
	}
	
	public void onTargetMouseEnter(DMouseEvent e) {
		if (timerHandle != null)
			timerHandle.close();
		
		timerHandle = context.setTimer(1000, this::show);
		enterMouseX = e.x;
		enterMouseY = e.y;
	}
	
	public void onTargetMouseMove(DMouseEvent e) {
		enterMouseX = e.x;
		enterMouseY = e.y;
	}
	
	public void onTargetMouseExit(DMouseEvent e) {
		if (timerHandle != null) {
			timerHandle.close();
			timerHandle = null;
		}
		
		hide();
	}
	
	private void show() {
		DSimpleTooltipComponent view = new DSimpleTooltipComponent(styleClass, tooltip);
		window = context.openView(enterMouseX, enterMouseY + context.dp(8), DAnchor.TOP_LEFT, view);
		
		if (timerHandle != null) {
			timerHandle.close();
			timerHandle = null;
		}
	}
	
	private void hide() {
		if (window != null) {
			window.close();
			window = null;
		}
	}
	
	public void close() {
		if (window != null) {
			window.close();
			window = null;
		}
	}
}
