/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.form;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DForm implements DComponent {
	private final DFormComponent[] components;
	private final DStyleClass styleClass;
	private final LiveObject<DDimensionPreferences> preferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	
	private DIRectangle bounds;
	private DUIContext context;
	private DFormStyle style;
	private DFontMetrics fontMetrics;
	
	public DForm(DStyleClass styleClass, DFormComponent... components) {
		this.styleClass = styleClass;
		this.components = components;
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("form", styleClass);
		style = new DFormStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.labelFont);
		for (DFormComponent component : components)
			component.component.setContext(path, context);
		
		int height = style.paddingBottom + style.paddingTop;
		int maxTextWidth = style.minimumLabelSize;
		int maxComponentWidth = style.minimumFieldSize;
		
		for (DFormComponent component : components) {
			maxTextWidth = Math.max(maxTextWidth, fontMetrics.getWidth(component.label));
			int componentWidth = component.component.getDimensionPreferences().getValue().preferredWidth;
			maxComponentWidth = Math.max(maxComponentWidth, componentWidth);
		}
		
		for (DFormComponent component : components) {
			height += component.component.getDimensionPreferences().getValue().preferredHeight;
			height += style.spacing;
		}
		
		preferences.setValue(new DDimensionPreferences(maxTextWidth + maxComponentWidth, height));
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return preferences;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		layout();
	}

	@Override
	public void paint(DCanvas canvas) {
		for (DFormComponent component : components) {
			
		}
	}

	@Override
	public void close() {
		
	}
	
	private void layout() {
		int x = bounds.x + style.paddingLeft;
		int y = bounds.y + style.paddingBottom;
		int maxTextWidth = style.minimumLabelSize;
		
		for (DFormComponent component : components)
			maxTextWidth = Math.max(maxTextWidth, fontMetrics.getWidth(component.label));
		
		for (DFormComponent component : components) {
			int preferredHeight = component.component.getDimensionPreferences().getValue().preferredHeight;
			DIRectangle componentBounds = new DIRectangle(x + maxTextWidth, y, bounds.width - maxTextWidth, preferredHeight);
			component.component.setBounds(componentBounds);
			
			y += preferredHeight + style.spacing;
		}
	}
}
