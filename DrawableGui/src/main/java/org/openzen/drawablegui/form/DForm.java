/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.form;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.openzen.drawablegui.BaseComponentGroup;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DForm extends BaseComponentGroup {
	private final DFormComponent[] components;
	private final DStyleClass styleClass;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private DIRectangle bounds;
	private DDrawSurface context;
	private DFormStyle style;
	private DFontMetrics fontMetrics;
	private int maxFieldWidth;
	private int maxLabelWidth;
	
	public DForm(DStyleClass styleClass, DFormComponent... components) {
		this.styleClass = styleClass;
		this.components = components;
	}

	@Override
	public void setSurface(DStylePath parent, int z, DDrawSurface surface) {
		this.context = surface;
		
		DStylePath path = parent.getChild("form", styleClass);
		style = new DFormStyle(surface.getStylesheet(path));
		fontMetrics = surface.getFontMetrics(style.labelFont);
		
		for (DFormComponent component : components)
			component.component.setSurface(path, z + 1, surface);
		
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
	public void paint(DCanvas canvas) {
		int x = style.paddingLeft;
		int y = style.paddingTop;
		for (DFormComponent component : components) {
			int baseline = component.component.getBaselineY();
			if (baseline == -1)
				baseline = fontMetrics.getAscent();
			canvas.drawText(style.labelFont, style.labelColor, x, y + baseline, component.label);
			component.component.paint(canvas);
			
			y += component.component.getSizing().getValue().preferredHeight + style.spacing;
		}
	}

	@Override
	public void close() {
		
	}
	
	private void layout() {
		int x = bounds.x + style.paddingLeft;
		int y = bounds.y + style.paddingBottom;
		
		for (DFormComponent component : components) {
			int preferredHeight = component.component.getSizing().getValue().preferredHeight;
			DIRectangle componentBounds = new DIRectangle(x + maxLabelWidth, y, bounds.width - maxLabelWidth - style.paddingLeft - style.paddingRight - style.spacing, preferredHeight);
			component.component.setBounds(componentBounds);
			
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
