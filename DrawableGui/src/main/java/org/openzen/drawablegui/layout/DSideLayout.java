/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.layout;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.openzen.drawablegui.BaseComponentGroup;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
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
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private DStylePath path;
	private DDrawSurface surface;
	private int z;
	private DSideLayoutStyle style;
	private DIRectangle bounds;
	
	private DDrawnRectangle background;
	
	public DSideLayout(DStyleClass styleClass, DComponent main) {
		this.styleClass = styleClass;
		this.main = main;
	}
	
	public void add(Side side, DComponent component) {
		if (surface != null)
			component.mount(path, z + 1, surface);
		
		sides.add(new SideComponent(side, component));
	}
	
	public void setMain(DComponent component) {
		if (this.main != null)
			this.main.close();
		
		this.main = component;
		
		if (surface != null && bounds != null) {
			main.mount(path, z + 1, surface);
			setBounds(bounds);
			surface.repaint(bounds);
		}
	}

	@Override
	public void mount(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		this.path = parent.getChild("sidelayout", styleClass);
		style = new DSideLayoutStyle(surface.getStylesheet(path));
		
		main.mount(path, z + 1, surface);
		for (SideComponent side : sides)
			side.component.mount(path, z + 1, surface);
		
		if (background != null)
			background.close();
		background = surface.fillRect(z, DIRectangle.EMPTY, style.backgroundColor);
	}
	
	@Override
	public void unmount() {
		main.unmount();
		for (SideComponent side : sides)
			side.component.unmount();
		
		background.close();
		background = null;
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		for (int i = sides.size() - 1; i >= 0; i--) {
			SideComponent side = sides.get(i);
			if (side.side == Side.TOP)
				return side.component.getBaselineY();
		}
		
		return main.getBaselineY();
	}

	@Override
	public LiveObject<DSizing> getSizing() {
		return sizing;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		
		background.setRectangle(bounds);
		
		int left = bounds.x;
		int right = bounds.x + bounds.width;
		int top = bounds.y;
		int bottom = bounds.y + bounds.height;
		for (int i = sides.size() - 1; i >= 0; i--) {
			SideComponent side = sides.get(i);
			DSizing preferences = side.component.getSizing().getValue();
			
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
	
	private void recalculateSize() {
		DSizing mainPreferences = main.getSizing().getValue();
		int minimumWidth = mainPreferences.minimumWidth;
		int minimumHeight = mainPreferences.minimumHeight;
		int preferredWidth = mainPreferences.preferredWidth;
		int preferredHeight = mainPreferences.preferredHeight;
		int maximumWidth = mainPreferences.maximumWidth;
		int maximumHeight = mainPreferences.maximumHeight;
		
		for (SideComponent side : sides) {
			DSizing sidePreferences = side.component.getSizing().getValue();
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
		
		sizing.setValue(new DSizing(
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
		
		background.close();
	}
	
	public class SideComponent implements Closeable, LiveObject.Listener<DSizing> {
		public final Side side;
		public final DComponent component;
		public final ListenerHandle<LiveObject.Listener<DSizing>> listenerHandle;

		public SideComponent(Side side, DComponent component) {
			this.side = side;
			this.component = component;
			listenerHandle = component.getSizing().addListener(this);
		}
		
		@Override
		public void close() {
			listenerHandle.close();
		}

		@Override
		public void onUpdated(DSizing oldValue, DSizing newValue) {
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
