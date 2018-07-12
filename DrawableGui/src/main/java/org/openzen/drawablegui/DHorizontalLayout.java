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
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class DHorizontalLayout extends BaseComponentGroup {
	private final DStyleClass styleClass;
	private final Alignment alignment;
	private final Element[] components;
	private final ListenerHandle<LiveObject.Listener<DSizing>>[] componentSizeListeners;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private DUIContext context;
	private DHorizontalLayoutStyle style;
	private DIRectangle bounds;
	private float totalGrow;
	private float totalShrink;
	
	public DHorizontalLayout(DStyleClass styleClass, Alignment alignment, Element... components) {
		this.styleClass = styleClass;
		this.alignment = alignment;
		this.components = components;
		
		componentSizeListeners = new ListenerHandle[components.length];
		totalGrow = 0;
		totalShrink = 0;
		
		for (int i = 0; i < componentSizeListeners.length; i++) {
			componentSizeListeners[i] = components[i].component.getSizing().addListener((oldValue, newValue) -> updateDimensionPreferences());
			
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
		
		DStylePath path = parent.getChild("horizontalLayout", styleClass);
		style = new DHorizontalLayoutStyle(context.getStylesheets().get(context, path));
		
		for (Element element : components)
			element.component.setContext(parent, context);
		
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
		LEFT(0),
		CENTER(0.5f),
		RIGHT(1);
		
		public final float align;
		
		Alignment(float align) {
			this.align = align;
		}
	}
	
	public static enum ElementAlignment {
		TOP,
		MIDDLE,
		BOTTOM,
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
		
		DSizing myPreferences = sizing.getValue();
		if (bounds.width < myPreferences.preferredWidth) {
			layoutShrinked();
		} else {
			layoutGrown();
		}
	}
	
	private void layoutShrinked() {
		DSizing myPreferences = sizing.getValue();
		if (totalShrink == 0) {
			// now what?
			// shrink proportionally, we have to shrink...
			float scale = (float)(bounds.width - (components.length - 1) * style.spacing) / (myPreferences.preferredWidth - (components.length - 1) * style.spacing);
			int x = 0;
			
			for (int i = 0; i < components.length; i++) {
				Element element = components[i];
				DSizing preferences = element.component.getSizing().getValue();
				int newX = x + preferences.preferredWidth;
				float idealUnspacedX = newX * scale;
				int idealX = (int)(idealUnspacedX + 0.5f + i * style.spacing);
				layout(element, x, idealX - x);
				x = idealX;
			}
		} else {
			int delta = bounds.width - myPreferences.preferredWidth;
			float deltaScaled = delta / totalShrink;
			int x = 0;
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				float scaledSize = preferences.preferredWidth + deltaScaled * element.shrink;
				float idealUnspacedX = x + scaledSize;
				int newX = (int)(idealUnspacedX + 0.5f);
				layout(element, x, newX - x);
				x = newX + style.spacing;
			}
		}
	}
	
	private void layoutGrown() {
		// resize according to grow values
		DSizing myPreferences = sizing.getValue();
		
		if (totalGrow == 0) {
			int deltaX = (int)(myPreferences.preferredWidth - bounds.width);
			int x = bounds.x + (int)(deltaX * alignment.align);
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				int newX = x + preferences.preferredWidth;
				layout(element, x, newX - x);
				x = newX + style.spacing;
			}
		} else {
			int delta = bounds.width - myPreferences.preferredWidth;
			float deltaScaled = delta / totalGrow;
			int x = 0;
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				float scaledSize = preferences.preferredWidth + deltaScaled * element.grow;
				float idealUnspacedX = x + scaledSize;
				int newX = (int)(idealUnspacedX + 0.5f);
				layout(element, x, newX - x);
				x = newX + style.spacing;
			}
		}
	}
	
	private void layout(Element element, int x, int width) {
		DSizing preferences = element.component.getSizing().getValue();
		int height;
		int y;
		switch (element.alignment) {
			case BOTTOM:
				height = Math.min(preferences.preferredHeight, bounds.height - style.paddingTop - style.paddingBottom);
				y = bounds.y + style.paddingTop;
				break;
			case MIDDLE:
				height = Math.min(preferences.preferredHeight, bounds.height - style.paddingTop - style.paddingBottom);
				y = bounds.y + style.paddingTop + (bounds.height - style.paddingTop - style.paddingBottom - height) / 2;
				break;
			case TOP:
				height = Math.min(preferences.preferredHeight, bounds.height - style.paddingTop - style.paddingBottom);
				y = bounds.y + bounds.height - style.paddingBottom - height;
				break;
			case STRETCH:
			default:
				height = bounds.height - style.paddingTop - style.paddingBottom;
				y = bounds.y + style.paddingTop;
				break;
		}
		element.component.setBounds(new DIRectangle(x, y, width, height));
	}
	
	private void updateDimensionPreferences() {
		int preferredWidth = -style.spacing;
		int preferredHeight = 0;
		int minimumWidth = -style.spacing;
		int minimumHeight = 0;
		int maximumWidth = -style.spacing;
		int maximumHeight = Integer.MAX_VALUE;
		for (Element element : components) {
			DSizing preferences = element.component.getSizing().getValue();
			preferredWidth += preferences.preferredWidth + style.spacing;
			preferredHeight = Math.max(preferredHeight, preferences.preferredHeight);
			
			minimumWidth += preferences.minimumWidth + style.spacing;
			minimumHeight = Math.max(minimumHeight, preferences.minimumHeight);
			
			maximumWidth += preferences.maximumWidth + style.spacing;
			maximumHeight = Math.min(maximumHeight, preferences.maximumHeight);
		}
		
		DSizing preferences = new DSizing(
				minimumWidth + style.paddingLeft + style.paddingRight,
				minimumHeight + style.paddingTop + style.paddingBottom,
				preferredWidth + style.paddingLeft + style.paddingRight,
				preferredHeight + style.paddingTop + style.paddingBottom,
				maximumWidth + style.paddingLeft + style.paddingRight,
				maximumHeight + style.paddingTop + style.paddingBottom);
		sizing.setValue(preferences);
	}
}
