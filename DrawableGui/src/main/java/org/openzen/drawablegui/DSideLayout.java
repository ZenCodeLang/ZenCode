/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

import org.openzen.drawablegui.listeners.DIRectangle;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
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
public class DSideLayout extends BaseComponentGroup {
	private final DStyleClass styleClass;
	
	private DComponent main;
	private final List<SideComponent> sides = new ArrayList<>();
	private final LiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	
	private DStylePath path;
	private DUIContext context;
	private DIRectangle bounds;
	
	public DSideLayout(DStyleClass styleClass, DComponent main) {
		this.styleClass = styleClass;
		this.main = main;
	}
	
	public void add(Side side, DComponent component) {
		if (context != null)
			component.setContext(path, context);
		
		sides.add(new SideComponent(side, component));
	}
	
	public void setMain(DComponent component) {
		if (this.main != null)
			this.main.close();
		
		this.main = component;
		main.setContext(path, context);
		setBounds(bounds);
		context.repaint(bounds);
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		this.path = parent.getChild("sidelayout", styleClass);
		
		main.setContext(path, context);
		for (SideComponent side : sides)
			side.component.setContext(path, context);
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		
		int left = bounds.x;
		int right = bounds.x + bounds.width;
		int top = bounds.y;
		int bottom = bounds.y + bounds.height;
		for (int i = sides.size() - 1; i >= 0; i--) {
			SideComponent side = sides.get(i);
			DDimensionPreferences preferences = side.component.getDimensionPreferences().getValue();
			
			switch (side.side) {
				case TOP: {
					int componentWidth = right - left;
					int componentHeight = preferences.preferredHeight;
					int componentX = left;
					int componentY = top;
					
					if (componentWidth > preferences.maximumWidth) {
						componentX += (componentWidth - preferences.maximumWidth) / 2;
						componentWidth = preferences.maximumWidth;
					}
					
					side.component.setBounds(new DIRectangle(componentX, componentY, componentWidth, componentHeight));
					top += componentHeight;
					break;
				}
				case BOTTOM: {
					int componentWidth = right - left;
					int componentHeight = preferences.preferredHeight;
					bottom -= componentHeight;
					int componentX = left;
					int componentY = bottom;
					
					if (componentWidth > preferences.maximumWidth) {
						componentX += (componentWidth - preferences.maximumWidth) / 2;
						componentWidth = preferences.maximumWidth;
					}
					
					side.component.setBounds(new DIRectangle(componentX, componentY, componentWidth, componentHeight));
					break;
				}
				case LEFT: {
					int componentWidth = preferences.preferredWidth;
					int componentHeight = bottom - top;
					int componentX = left;
					int componentY = top;
					
					if (componentHeight > preferences.maximumHeight) {
						componentY += (componentHeight - preferences.maximumHeight) / 2;
						componentHeight = preferences.maximumHeight;
					}
					
					side.component.setBounds(new DIRectangle(componentX, componentY, componentWidth, componentHeight));
					left += componentWidth;
					break;
				}
				case RIGHT: {
					int componentWidth = preferences.preferredWidth;
					int componentHeight = bottom - top;
					right -= componentWidth;
					int componentX = right;
					int componentY = top;
					
					if (componentHeight > preferences.maximumHeight) {
						componentY += (componentHeight - preferences.maximumHeight) / 2;
						componentHeight = preferences.maximumHeight;
					}
					
					side.component.setBounds(new DIRectangle(componentX, componentY, componentWidth, componentHeight));
					break;
				}
			}
		}
		
		main.setBounds(new DIRectangle(left, top, right - left, bottom - top));
	}

	@Override
	public void paint(DCanvas canvas) {
		main.paint(canvas);
		
		for (SideComponent component : sides)
			component.component.paint(canvas);
	}
	
	private void recalculateSize() {
		DDimensionPreferences mainPreferences = main.getDimensionPreferences().getValue();
		int minimumWidth = mainPreferences.minimumWidth;
		int minimumHeight = mainPreferences.minimumHeight;
		int preferredWidth = mainPreferences.preferredWidth;
		int preferredHeight = mainPreferences.preferredHeight;
		int maximumWidth = mainPreferences.maximumWidth;
		int maximumHeight = mainPreferences.maximumHeight;
		
		for (SideComponent side : sides) {
			DDimensionPreferences sidePreferences = side.component.getDimensionPreferences().getValue();
			switch (side.side) {
				case LEFT:
				case RIGHT:
					minimumWidth += sidePreferences.preferredWidth;
					preferredWidth += sidePreferences.preferredWidth;
					maximumWidth += sidePreferences.preferredWidth;
					
					minimumHeight = Math.max(minimumHeight, sidePreferences.minimumHeight);
					preferredHeight = Math.max(maximumHeight, sidePreferences.preferredHeight);
					break;
				case BOTTOM:
				case TOP:
					minimumHeight += sidePreferences.preferredHeight;
					preferredHeight += sidePreferences.preferredHeight;
					maximumHeight += sidePreferences.preferredHeight;
					
					minimumWidth = Math.max(minimumWidth, sidePreferences.minimumWidth);
					preferredWidth = Math.max(preferredWidth, sidePreferences.preferredWidth);
					break;
			}
		}
		
		dimensionPreferences.setValue(new DDimensionPreferences(
				minimumWidth,
				minimumHeight,
				preferredWidth,
				preferredHeight,
				maximumWidth,
				maximumHeight));
	}

	@Override
	protected void forEachChild(Consumer<DComponent> children) {
		children.accept(main);
		for (SideComponent side : sides)
			children.accept(side.component);
	}
	
	@Override
	public DComponent findChild(Predicate<DComponent> predicate) {
		if (predicate.test(main))
			return main;
		for (SideComponent side : sides)
			if (predicate.test(side.component))
				return side.component;
		
		return null;
	}

	@Override
	public void close() {
		main.close();
		for (SideComponent side : sides)
			side.close();
	}
	
	public class SideComponent implements Closeable, LiveObject.Listener<DDimensionPreferences> {
		public final Side side;
		public final DComponent component;
		public final ListenerHandle<LiveObject.Listener<DDimensionPreferences>> listenerHandle;

		public SideComponent(Side side, DComponent component) {
			this.side = side;
			this.component = component;
			listenerHandle = component.getDimensionPreferences().addListener(this);
		}
		
		@Override
		public void close() {
			listenerHandle.close();
		}

		@Override
		public void onUpdated(DDimensionPreferences oldValue, DDimensionPreferences newValue) {
			recalculateSize();
		}
	}
	
	public enum Side {
		LEFT,
		RIGHT,
		TOP,
		BOTTOM
	}
}
