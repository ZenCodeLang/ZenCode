/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.form;

import java.util.function.Consumer;
import java.util.function.Predicate;
import live.LiveObject;
import live.MutableLiveObject;

import org.openzen.drawablegui.BaseComponentGroup;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class DForm extends BaseComponentGroup {
	private final DFormComponent[] components;
	private final DStyleClass styleClass;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private DComponentContext context;
	private DIRectangle bounds;
	private DFormStyle style;
	private DFontMetrics fontMetrics;
	private int maxFieldWidth;
	private int maxLabelWidth;
	
	private final DDrawnText[] labels;
	
	public DForm(DStyleClass styleClass, DFormComponent... components) {
		this.styleClass = styleClass;
		this.components = components;
		
		labels = new DDrawnText[components.length];
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("form", styleClass);
		style = context.getStyle(DFormStyle::new);
		fontMetrics = context.getFontMetrics(style.labelFont);
		
		for (DFormComponent component : components)
			component.component.mount(context);
		
		int height = style.paddingBottom + style.paddingTop;
		int maxLabelWidth = style.minimumLabelSize;
		int maxFieldWidth = style.minimumFieldSize;
		
		for (DFormComponent component : components) {
			maxLabelWidth = Math.max(maxLabelWidth, fontMetrics.getWidth(component.label));
			int componentWidth = component.component.getSizing().getValue().preferredWidth;
			maxFieldWidth = Math.max(maxFieldWidth, componentWidth);
		}
		
		for (DFormComponent component : components) {
			height += component.component.getSizing().getValue().preferredHeight;
			height += style.spacing;
		}
		
		this.maxFieldWidth = maxFieldWidth;
		this.maxLabelWidth = maxLabelWidth;
		sizing.setValue(new DSizing(maxLabelWidth + maxFieldWidth + style.paddingLeft + style.paddingRight + style.spacing, height));
		
		if (bounds != null)
			layout();
		
		for (int i = 0; i < labels.length; i++) {
			if (labels[i] != null)
				labels[i].close();
			labels[i] = parent.drawText(1, style.labelFont, style.labelColor, 0, 0, components[i].label);
		}
	}
	
	@Override
	public void unmount() {
		for (int i = 0; i < labels.length; i++) {
			if (labels[i] == null)
				continue;
			
			labels[i].close();
			labels[i] = null;
		}
		
		for (DFormComponent component : components)
			component.component.unmount();
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
		int contentBaseline = components[0].component.getBaselineY();
		return contentBaseline == -1 ? -1 : contentBaseline + style.paddingTop;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		
		if (context != null)
			layout();
	}

	@Override
	public void close() {
		unmount();
	}
	
	private void layout() {
		int x = bounds.x + style.paddingLeft;
		int y = bounds.y + style.paddingBottom;
		
		for (int i = 0; i < components.length; i++) {
			DFormComponent component = components[i];
			
			int preferredHeight = component.component.getSizing().getValue().preferredHeight;
			DIRectangle componentBounds = new DIRectangle(x + maxLabelWidth, y, bounds.width - maxLabelWidth - style.paddingLeft - style.paddingRight - style.spacing, preferredHeight);
			component.component.setBounds(componentBounds);
			
			int baseline = component.component.getBaselineY();
			if (baseline == -1)
				baseline = fontMetrics.getAscent();
			labels[i].setPosition(x, y + baseline);
			
			y += preferredHeight + style.spacing;
		}
	}

	@Override
	protected void forEachChild(Consumer<DComponent> children) {
		for (DFormComponent component : components)
			children.accept(component.component);
	}

	@Override
	protected DComponent findChild(Predicate<DComponent> predicate) {
		for (DFormComponent component : components)
			if (predicate.test(component.component))
				return component.component;
		
		return null;
	}
}
