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
public class DLinearLayout extends BaseComponentGroup {
	private final DStyleClass styleClass;
	private final Orientation orientation;
	private final Alignment alignment;
	private final Element[] components;
	private final ListenerHandle<LiveObject.Listener<DSizing>>[] componentSizeListeners;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private DUIContext context;
	private DLinearLayoutStyle style;
	private DIRectangle bounds;
	private float totalGrow;
	private float totalShrink;
	private DPath shape;
	
	public DLinearLayout(DStyleClass styleClass, Orientation orientation, Alignment alignment, Element... components) {
		this.styleClass = styleClass;
		this.orientation = orientation;
		this.alignment = alignment;
		this.components = components;
		
		componentSizeListeners = new ListenerHandle[components.length];
		totalGrow = 0;
		totalShrink = 0;
		
		for (int i = 0; i < componentSizeListeners.length; i++) {
			componentSizeListeners[i] = components[i].component.getSizing().addListener((oldValue, newValue) -> updateSizing());
			
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
		style = new DLinearLayoutStyle(context.getStylesheets().get(context, path));
		
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
		shape = style.shape.instance(style.margin.apply(bounds));
		layout();
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.shadowPath(shape, DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
		style.border.paint(canvas, bounds);
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
	
	private void layout() {
		if (orientation == Orientation.HORIZONTAL) {
			layoutHorizontal();
		} else {
			layoutVertical();
		}
	}
	
	private void layoutHorizontal() {
		if (bounds == null || context == null)
			return;
		
		DSizing myPreferences = sizing.getValue();
		if (bounds.width < myPreferences.preferredWidth) {
			layoutHorizontalShrinked();
		} else {
			layoutHorizontalGrown();
		}
	}
	
	private void layoutVertical() {
		if (bounds == null || context == null)
			return;
		
		DSizing myPreferences = sizing.getValue();
		if (bounds.height < myPreferences.preferredHeight) {
			layoutVerticalShrinked();
		} else {
			layoutVerticalGrown();
		}
	}
	
	private void layoutHorizontalShrinked() {
		float availableWidth = getInnerWidth() - (components.length - 1) * style.spacing;
		float preferredInnerWidth = getPreferredInnerWidth() - (components.length - 1) * style.spacing;
		
		if (totalShrink == 0) {
			// now what?
			// shrink proportionally, we have to shrink...
			float scale = availableWidth / preferredInnerWidth;
			int x = bounds.x + style.margin.left + style.border.getPaddingLeft();
			
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				int newX = (int)(x + preferences.preferredWidth * scale + 0.5f);
				layoutHorizontal(element, bounds.x + x, newX - x);
				x = newX + style.spacing;
			}
		} else {
			int delta = getInnerHeight() - getPreferredInnerHeight();
			float deltaScaled = delta / totalShrink;
			int y = bounds.y;
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				float scaledSize = preferences.preferredHeight + deltaScaled * element.shrink;
				float idealUnspacedY = y + scaledSize;
				int newX = (int)(idealUnspacedY + 0.5f);
				layoutHorizontal(element, y, newX - y);
				y = newX + style.spacing;
			}
		}
	}
	
	private void layoutVerticalShrinked() {
		float availableHeight = getInnerHeight() - (components.length - 1) * style.spacing;
		float preferredInnerHeight = getPreferredInnerHeight() - (components.length - 1) * style.spacing;
		
		if (totalShrink == 0) {
			// now what?
			// shrink proportionally, we have to shrink...
			float scale = availableHeight / preferredInnerHeight;
			int y = bounds.y + style.margin.top + style.border.getPaddingTop();
			
			for (int i = 0; i < components.length; i++) {
				Element element = components[i];
				DSizing preferences = element.component.getSizing().getValue();
				int newY = (int)(y + preferences.preferredHeight * scale + 0.5f);
				layoutVertical(element, bounds.y + y, newY - y);
				y = newY + style.spacing;
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
				layoutVertical(element, y, newY - y);
				y = newY + style.spacing;
			}
		}
	}
	
	private void layoutHorizontalGrown() {
		// resize according to grow values
		int delta = getPreferredInnerWidth() - getInnerWidth();
		if (totalGrow == 0) {
			int x = bounds.x + style.margin.left + style.border.getPaddingLeft() + (int)(delta * alignment.align);
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				int newX = x + preferences.preferredWidth;
				layoutHorizontal(element, x, newX - x);
				x = newX;
			}
		} else {
			float deltaScaled = delta / totalGrow;
			int x = bounds.x + style.margin.left + style.border.getPaddingLeft();
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				float scaledSize = preferences.preferredWidth - deltaScaled * element.grow;
				float idealUnspacedX = x + scaledSize;
				int newX = (int)(idealUnspacedX + 0.5f);
				layoutHorizontal(element, x, newX - x);
				x = newX + style.spacing;
			}
		}
	}
	
	private void layoutVerticalGrown() {
		// resize according to grow values
		int delta = getPreferredInnerHeight() - getInnerHeight();
		if (totalGrow == 0) {
			int y = bounds.y + style.margin.top + style.border.getPaddingTop() + (int)(delta * alignment.align);
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				int newY = y + preferences.preferredHeight;
				layoutVertical(element, y, newY - y);
				y = newY;
			}
		} else {
			float deltaScaled = delta / totalGrow;
			int y = bounds.y + style.margin.top + style.border.getPaddingTop();
			for (Element element : components) {
				DSizing preferences = element.component.getSizing().getValue();
				float scaledSize = preferences.preferredHeight - deltaScaled * element.grow;
				float idealUnspacedY = y + scaledSize;
				int newY = (int)(idealUnspacedY + 0.5f);
				layoutVertical(element, y, newY - y);
				y = newY + style.spacing;
			}
		}
	}
	
	private void layoutHorizontal(Element element, int x, int width) {
		DSizing preferences = element.component.getSizing().getValue();
		int height;
		int y;
		switch (element.alignment) {
			case BOTTOM:
				height = Math.min(preferences.preferredHeight, bounds.height - style.border.getPaddingVertical());
				y = bounds.y + style.border.getPaddingTop();
				break;
			case MIDDLE:
				height = Math.min(preferences.preferredHeight, bounds.height - style.border.getPaddingVertical());
				y = bounds.y + style.border.getPaddingTop() + (bounds.height - style.border.getPaddingVertical() - height) / 2;
				break;
			case TOP:
				height = Math.min(preferences.preferredHeight, bounds.height - style.border.getPaddingVertical());
				y = bounds.y + bounds.height - style.border.getPaddingBottom() - height;
				break;
			case STRETCH:
			default:
				height = bounds.height - style.border.getPaddingVertical();
				y = bounds.y + style.border.getPaddingTop();
				break;
		}
		element.component.setBounds(new DIRectangle(x, y, width, height));
	}
	
	private void layoutVertical(Element element, int y, int height) {
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
	
	private void updateSizing() {
		if (orientation == Orientation.HORIZONTAL) {
			updateSizingHorizontal();
		} else {
			updateSizingVertical();
		}
	}
	
	private void updateSizingHorizontal() {
		int preferredWidth = -style.spacing;
		int preferredHeight = 0;
		int minimumWidth = -style.spacing;
		int minimumHeight = Integer.MAX_VALUE;
		int maximumWidth = -style.spacing;
		int maximumHeight = 0;
		
		for (Element element : components) {
			DSizing preferences = element.component.getSizing().getValue();
			preferredWidth += preferences.preferredWidth + style.spacing;
			preferredHeight = Math.max(preferredHeight, preferences.preferredHeight);
			
			minimumWidth += preferences.minimumWidth + style.spacing;
			minimumHeight = Math.max(minimumHeight, preferences.minimumHeight);
			
			maximumHeight += preferences.maximumWidth + style.spacing;
			maximumWidth = Math.min(maximumHeight, preferences.maximumHeight);
		}
		
		int paddingHorizontal = style.border.getPaddingHorizontal() + style.margin.getHorizontal();
		int paddingVertical = style.border.getPaddingVertical() + style.margin.getVertical();
		DSizing preferences = new DSizing(
				minimumWidth + paddingHorizontal,
				minimumHeight + paddingVertical,
				preferredWidth + paddingHorizontal,
				preferredHeight + paddingVertical,
				maximumWidth + paddingHorizontal,
				maximumHeight + paddingVertical);
		sizing.setValue(preferences);
	}
	
	private void updateSizingVertical() {
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
		
		int paddingHorizontal = style.border.getPaddingHorizontal() + style.margin.getHorizontal();
		int paddingVertical = style.border.getPaddingVertical() + style.margin.getVertical();
		DSizing preferences = new DSizing(
				minimumWidth + paddingHorizontal,
				minimumHeight + paddingVertical,
				preferredWidth + paddingHorizontal,
				preferredHeight + paddingVertical,
				maximumWidth + paddingHorizontal,
				maximumHeight + paddingVertical);
		sizing.setValue(preferences);
	}
	
	public static enum Orientation {
		HORIZONTAL,
		VERTICAL
	}
	
	public static enum Alignment {
		TOP(0),
		LEFT(0),
		MIDDLE(0.5f),
		CENTER(0.5f),
		BOTTOM(1),
		RIGHT(1);
		
		public final float align;
		
		Alignment(float align) {
			this.align = align;
		}
	}
	
	public static enum ElementAlignment {
		LEFT,
		CENTER,
		RIGHT,
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
}
