/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DVerticalLayout extends BaseComponentGroup {
	private final DStyleClass styleClass;
	private final Alignment alignment;
	private final Element[] components;
	private final ListenerHandle<LiveObject.Listener<DDimensionPreferences>>[] componentSizeListeners;
	private final LiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	
	private DUIContext context;
	private DHorizontalLayoutStyle style;
	private DIRectangle bounds;
	private float totalGrow;
	private float totalShrink;
	
	public DVerticalLayout(DStyleClass styleClass, Alignment alignment, Element... components) {
		this.styleClass = styleClass;
		this.alignment = alignment;
		this.components = components;
		
		componentSizeListeners = new ListenerHandle[components.length];
		totalGrow = 0;
		totalShrink = 0;
		
		for (int i = 0; i < componentSizeListeners.length; i++) {
			componentSizeListeners[i] = components[i].component.getDimensionPreferences().addListener((oldValue, newValue) -> updateDimensionPreferences());
			
			totalGrow += components[i].grow;
			totalShrink += components[i].shrink;
		}
	}

	@Override
	protected void forEachChild(Consumer<DComponent> children) {
		for (Element element : components)
			children.accept(element.component);
	}

	@Override
	protected DComponent findChild(Predicate<DComponent> predicate) {
		for (Element element : components)
			if (predicate.test(element.component))
				return element.component;
		
		return null;
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("verticalLayout", styleClass);
		style = new DHorizontalLayoutStyle(context.getStylesheets().get(context, path));
		
		for (Element element : components)
			element.component.setContext(parent, context);
		
		layout();
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public int getBaselineY() {
		return components[0].component.getBaselineY();
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		layout();
	}

	@Override
	public void paint(DCanvas canvas) {
		for (Element element : components) {
			element.component.paint(canvas);
		}
	}

	@Override
	public void close() {
		for (int i = 0; i < componentSizeListeners.length; i++) {
			componentSizeListeners[i].close();
		}
	}
	
	public static enum Alignment {
		TOP(0),
		MIDDLE(0.5f),
		BOTTOM(1);
		
		public final float align;
		
		Alignment(float align) {
			this.align = align;
		}
	}
	
	public static enum ElementAlignment {
		LEFT,
		CENTER,
		RIGHT,
		STRETCH
	}
	
	public static class Element {
		public final DComponent component;
		public final float grow;
		public final float shrink;
		public final ElementAlignment alignment;
		
		public Element(DComponent component, float grow, float shrink, ElementAlignment alignment) {
			this.component = component;
			this.grow = grow;
			this.shrink = shrink;
			this.alignment = alignment;
		}
	}
	
	private void layout() {
		if (bounds == null || context == null)
			return;
		
		DDimensionPreferences myPreferences = dimensionPreferences.getValue();
		if (bounds.height < myPreferences.preferredHeight) {
			layoutShrinked();
		} else {
			layoutGrown();
		}
	}
	
	private void layoutShrinked() {
		DDimensionPreferences myPreferences = dimensionPreferences.getValue();
		if (totalShrink == 0) {
			// now what?
			// shrink proportionally, we have to shrink...
			float scale = (float)(bounds.height - (components.length - 1) * style.spacing) / (myPreferences.preferredHeight - (components.length - 1) * style.spacing);
			int y = 0;
			
			for (int i = 0; i < components.length; i++) {
				Element element = components[i];
				DDimensionPreferences preferences = element.component.getDimensionPreferences().getValue();
				int newY = y + preferences.preferredHeight;
				float idealUnspacedY = newY * scale;
				int idealY = (int)(idealUnspacedY + 0.5f + i * style.spacing);
				layout(element, bounds.y + y, idealY - y);
				y = idealY;
			}
		} else {
			int delta = bounds.height - myPreferences.preferredHeight;
			float deltaScaled = delta / totalShrink;
			int y = bounds.y;
			for (Element element : components) {
				DDimensionPreferences preferences = element.component.getDimensionPreferences().getValue();
				float scaledSize = preferences.preferredHeight + deltaScaled * element.shrink;
				float idealUnspacedY = y + scaledSize;
				int newY = (int)(idealUnspacedY + 0.5f);
				layout(element, y, newY - y);
				y = newY + style.spacing;
			}
		}
	}
	
	private void layoutGrown() {
		// resize according to grow values
		DDimensionPreferences myPreferences = dimensionPreferences.getValue();
		
		if (totalGrow == 0) {
			int deltaY = (int)(myPreferences.preferredHeight - bounds.height);
			int y = bounds.y + (int)(deltaY * alignment.align);
			for (Element element : components) {
				DDimensionPreferences preferences = element.component.getDimensionPreferences().getValue();
				int newY = y + preferences.preferredHeight;
				layout(element, y, newY - y);
				y = newY;
			}
		} else {
			int delta = bounds.width - myPreferences.preferredWidth;
			float deltaScaled = delta / totalGrow;
			int y = bounds.y;
			for (Element element : components) {
				DDimensionPreferences preferences = element.component.getDimensionPreferences().getValue();
				float scaledSize = preferences.preferredHeight + deltaScaled * element.grow;
				float idealUnspacedY = y + scaledSize;
				int newY = (int)(idealUnspacedY + 0.5f);
				layout(element, y, newY - y);
				y = newY + style.spacing;
			}
		}
	}
	
	private void layout(Element element, int y, int height) {
		DDimensionPreferences preferences = element.component.getDimensionPreferences().getValue();
		int x;
		int width;
		switch (element.alignment) {
			case LEFT:
				width = Math.min(preferences.preferredWidth, bounds.width - style.paddingLeft - style.paddingRight);
				x = bounds.x + style.paddingLeft;
				break;
			case CENTER:
				width = Math.min(preferences.preferredWidth, bounds.width - style.paddingLeft - style.paddingRight);
				x = bounds.x + style.paddingLeft + (bounds.width - style.paddingLeft - style.paddingRight - width) / 2;
				break;
			case RIGHT:
				width = Math.min(preferences.preferredWidth, bounds.width - style.paddingLeft - style.paddingRight);
				x = bounds.x + bounds.width - style.paddingLeft - width;
				break;
			case STRETCH:
			default:
				width = bounds.width - style.paddingLeft - style.paddingRight;
				x = bounds.x + style.paddingLeft;
				break;
		}
		element.component.setBounds(new DIRectangle(x, y, width, height));
	}
	
	private void updateDimensionPreferences() {
		int preferredWidth = 0;
		int preferredHeight = -style.spacing;
		int minimumWidth = 0;
		int minimumHeight = -style.spacing;
		int maximumWidth = Integer.MAX_VALUE;
		int maximumHeight = -style.spacing;
		
		for (Element element : components) {
			DDimensionPreferences preferences = element.component.getDimensionPreferences().getValue();
			preferredWidth = Math.max(preferredWidth, preferences.preferredWidth);
			preferredHeight += preferences.preferredHeight + style.spacing;
			
			minimumWidth = Math.max(minimumWidth, preferences.minimumWidth);
			minimumHeight += preferences.minimumHeight + style.spacing;
			
			maximumWidth = Math.min(maximumWidth, preferences.maximumWidth);
			maximumHeight += preferences.maximumHeight + style.spacing;
		}
		
		DDimensionPreferences preferences = new DDimensionPreferences(
				minimumWidth + style.paddingLeft + style.paddingRight,
				minimumHeight + style.paddingTop + style.paddingBottom,
				preferredWidth + style.paddingLeft + style.paddingRight,
				preferredHeight + style.paddingTop + style.paddingBottom,
				maximumWidth + style.paddingLeft + style.paddingRight,
				maximumHeight + style.paddingTop + style.paddingBottom);
		dimensionPreferences.setValue(preferences);
	}
}
