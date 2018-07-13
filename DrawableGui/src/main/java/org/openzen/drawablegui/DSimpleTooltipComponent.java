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
public class DSimpleTooltipComponent implements DComponent {
	private final DStyleClass styleClass;
	private final LiveString tooltip;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final ListenerHandle<LiveString.Listener> tooltipListener;
	
	private DUIContext context;
	private DIRectangle bounds;
	private DFontMetrics fontMetrics;
	private DSimpleTooltipStyle style;
	
	public DSimpleTooltipComponent(DStyleClass styleClass, LiveString tooltip) {
		this.styleClass = styleClass;
		this.tooltip = tooltip;
		tooltipListener = tooltip.addListener(this::onTooltipChanged);
	}
	
	private void onTooltipChanged(String oldValue, String newValue) {
		if (context == null || bounds == null)
			return;
		
		calculateSize();
		bounds = new DIRectangle(
				bounds.x,
				bounds.y,
				style.border.getPaddingLeft() + fontMetrics.getWidth(tooltip.getValue()) + style.border.getPaddingRight(),
				style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingBottom());
		context.repaint(bounds);
	}
	
	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
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
	public LiveObject<DSizing> getSizing() {
		return sizing;
	}
	
	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("tooltip", styleClass);
		style = new DSimpleTooltipStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.font);
		calculateSize();
	}
	
	@Override
	public void paint(DCanvas canvas) {
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		style.border.paint(canvas, bounds);
		canvas.drawText(
				style.font,
				style.textColor,
				bounds.x + style.border.getPaddingLeft(),
				bounds.y + style.border.getPaddingTop() + fontMetrics.getAscent(),
				tooltip.getValue());
	}
	
	@Override
	public void close() {
		tooltipListener.close();
	}
	
	private void calculateSize() {
		sizing.setValue(new DSizing(
				style.border.getPaddingLeft() + fontMetrics.getWidth(tooltip.getValue()) + style.border.getPaddingRight(),
				style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingBottom()));
	}
}
