/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.layout;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.openzen.drawablegui.BaseComponentGroup;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
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
	private final ListenerHandle<LiveObject.Listener<DSizing>>[] componentSizeListeners;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private DUIContext context;
	private DVerticalLayoutStyle style;
	private DIRectangle bounds;
	private float totalGrow;
	private float totalShrink;
	private DPath shape;
	
	public DVerticalLayout(DStyleClass styleClass, Alignment alignment, Element... components) {
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
		
		DStylePath path = parent.getChild("verticalLayout", styleClass);
		style = new DVerticalLayoutStyle(context.getStylesheets().get(context, path));
		
		for (Element element : components)
			element.component.setContext(parent, context);
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
		shape = DPath.roundedRectangle(
				bounds.x + style.margin.left,
				bounds.y + style.margin.top,
				bounds.width - style.margin.getHorizontal(),
				bounds.height - style.margin.getVertical(),
				style.cornerRadius);
		layout();
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.shadowPath(shape, DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
		for (Element element : components) {
			element.component.paint(canvas);
		}
	}

	@Override
	public void close() {
		for (ListenerHandle<LiveObject.Listener<DSizing>> listener : componentSizeListeners) {
			listener.close();
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
		
		DSizing myPreferences = sizing.getValue();
		if (bounds.height < myPreferences.preferredHeight) {
			layoutShrinked();
		} else {
			layoutGrown();
		}
	}
	
	private int getInnerWidth() {
		return bounds.width - style.margin.getHorizontal() - style.border.getPaddingHorizontal();
	}
	
	private int getInnerHeight() {
		return bounds.height - style.margin.getVertical() - style.border.getPaddingVertical();
	}
	
	private int getPreferredInnerWidth() {
		DSizing myPreferences = sizing.getValue();
		return myPreferences.preferredWidth - style.margin.getHorizontal() - style.border.getPaddingHorizontal();
	}
	
	private int getPreferredInnerHeight() {
		DSizing myPreferences = sizing.getValue();
		return myPreferences.preferredHeight - style.margin.getVertical() - style.border.getPaddingVertical();
	}
	
	private void layoutShrinked() {
		if (totalShrink == 0) {
			// now what?
			// shrink proportionally, we have to shrink...
			float availableHeight = getInnerHeight() - (components.length - 1) * style.spacing;
			float scale = (float)(availableHeight) / (getPreferredInnerHeight() - (components.length - 1) * style.spacing);
			int y = bounds.y + style.margin.top + style.border.getPaddingTop();
			
			for (int i = 0; i < components.length; i++) {
				Element element = components[i];
				DSizing preferences = element.component.getSizing().getValue();
				int newY = y + preferences.preferredHeight;
				float idealUnspacedY = newY * scale;
				int idealY = (int)(idealUnspacedY + 0.5f + i * style.spacing);
				layout(element, bounds.y + y, idealY - y);
				y = idealY;
			}
		} else {
			int delta = getInnerHeight() - getPreferredInnerHeight();
			float deltaScaled = delta / totalShrink;
			int y = bounds.y;
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
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
		if (totalGrow == 0) {
			int deltaY = getPreferredInnerHeight() - getInnerHeight();
			int y = bounds.y + style.margin.top + style.border.getPaddingTop() + (int)(deltaY * alignment.align);
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				int newY = y + preferences.preferredHeight;
				layout(element, y, newY - y);
				y = newY;
			}
		} else {
			int delta = getPreferredInnerHeight() - getInnerHeight();
			float deltaScaled = delta / totalGrow;
			int y = bounds.y + style.margin.top + style.border.getPaddingTop();
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				float scaledSize = preferences.preferredHeight + deltaScaled * element.grow;
				float idealUnspacedY = y + scaledSize;
				int newY = (int)(idealUnspacedY + 0.5f);
				layout(element, y, newY - y);
				y = newY + style.spacing;
			}
		}
	}
	
	private void layout(Element element, int y, int height) {
		DSizing preferences = element.component.getSizing().getValue();
		int x;
		int width;
		switch (element.alignment) {
			case LEFT:
				width = Math.min(preferences.preferredWidth, bounds.width - style.border.getPaddingHorizontal() - style.margin.getHorizontal());
				x = bounds.x + style.margin.left + style.border.getPaddingLeft();
				break;
			case CENTER:
				width = Math.min(preferences.preferredWidth, bounds.width - style.border.getPaddingHorizontal());
				x = bounds.x + style.margin.left + style.border.getPaddingLeft() + (bounds.width - style.border.getPaddingHorizontal() - width) / 2;
				break;
			case RIGHT:
				width = Math.min(preferences.preferredWidth, bounds.width - style.border.getPaddingHorizontal());
				x = bounds.x + bounds.width - style.margin.right - style.border.getPaddingRight() - width;
				break;
			case STRETCH:
			default:
				width = bounds.width - style.border.getPaddingHorizontal() - style.margin.getHorizontal();
				x = bounds.x + style.margin.left + style.border.getPaddingLeft();
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
			DSizing preferences = element.component.getSizing().getValue();
			preferredWidth = Math.max(preferredWidth, preferences.preferredWidth);
			preferredHeight += preferences.preferredHeight + style.spacing;
			
			minimumWidth = Math.max(minimumWidth, preferences.minimumWidth);
			minimumHeight += preferences.minimumHeight + style.spacing;
			
			maximumWidth = Math.min(maximumWidth, preferences.maximumWidth);
			maximumHeight += preferences.maximumHeight + style.spacing;
		}
		
		int paddingHorizontal = style.border.getPaddingHorizontal();
		int paddingVertical = style.border.getPaddingVertical();
		DSizing preferences = new DSizing(
				minimumWidth + paddingHorizontal,
				minimumHeight + paddingVertical,
				preferredWidth + paddingHorizontal,
				preferredHeight + paddingVertical,
				maximumWidth + paddingHorizontal,
				maximumHeight + paddingVertical);
		sizing.setValue(preferences);
	}
}
