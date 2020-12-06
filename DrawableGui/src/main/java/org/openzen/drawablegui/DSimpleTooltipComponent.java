/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.util.function.BiConsumer;

import listeners.ListenerHandle;
import live.LiveObject;
import live.LiveString;
import live.MutableLiveObject;

import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.style.DStyleClass;

/**
 * @author Hoofdgebruiker
 */
public class DSimpleTooltipComponent implements DComponent {
	private final DStyleClass styleClass;
	private final LiveString tooltip;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final ListenerHandle<BiConsumer<String, String>> tooltipListener;

	private DComponentContext context;
	private DIRectangle bounds;
	private DFontMetrics fontMetrics;
	private DSimpleTooltipStyle style;

	private DDrawnRectangle background;
	private DDrawnText text;

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

		if (text != null)
			text.close();

		text = context.drawText(
				0,
				style.font,
				style.textColor,
				bounds.x + style.border.getPaddingLeft(),
				bounds.y + style.border.getPaddingTop() + fontMetrics.getAscent(),
				newValue);
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		style.border.update(context, bounds);

		if (background != null)
			background.close();
		background = context.fillRect(0, bounds, style.backgroundColor);
		text.setPosition(
				bounds.x + style.border.getPaddingLeft(),
				bounds.y + style.border.getPaddingTop() + fontMetrics.getAscent());
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
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("tooltip", styleClass);
		style = context.getStyle(DSimpleTooltipStyle::new);
		fontMetrics = context.getFontMetrics(style.font);
		calculateSize();

		if (text != null)
			text.close();
		text = parent.drawText(1, style.font, style.textColor, 0, 0, tooltip.getValue());
	}

	@Override
	public void unmount() {
		if (background != null)
			background.close();
		if (text != null)
			text.close();
	}

	@Override
	public void close() {
		tooltipListener.close();
		unmount();
	}

	private void calculateSize() {
		sizing.setValue(new DSizing(
				style.border.getPaddingLeft() + fontMetrics.getWidth(tooltip.getValue()) + style.border.getPaddingRight(),
				style.border.getPaddingTop() + fontMetrics.getAscent() + fontMetrics.getDescent() + style.border.getPaddingBottom()));
	}
}
