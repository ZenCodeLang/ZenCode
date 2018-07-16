/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.openzen.drawablegui.BaseComponentGroup;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveArrayList;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveList;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedView extends BaseComponentGroup {
	private final DStyleClass styleClass;
	public final MutableLiveList<TabbedViewComponent> tabs = new LiveArrayList<>();
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	public final MutableLiveObject<TabbedViewComponent> currentTab = new SimpleLiveObject<>(null);
	
	private final Map<TabbedViewTab, ListenerHandle<LiveObject.Listener<DSizing>>> tabSizeListeners = new HashMap<>();
	
	private DUIContext context;
	private DStylePath path;
	private DIRectangle bounds;
	private TabbedViewStyle style;
	private int totalTabHeight;
	private DFontMetrics fontMetrics;

	private final LiveList<TabbedViewTab> tabComponents = new LiveMappedList<>(tabs, tab -> {
		TabbedViewTab result = new TabbedViewTab(this, currentTab, tab);
		if (context != null)
			result.setContext(path, context);
		
		tabSizeListeners.put(result, result.getSizing().addListener((oldSize, newSize) -> layoutTabs()));
		return result;
	});
	
	public TabbedView(DStyleClass styleClass) {
		this.styleClass = styleClass;
		tabs.addListener(new TabListListener());
		
		currentTab.addListener((oldValue, newValue) -> {
			if (oldValue != null)
				oldValue.content.onUnmounted();
			if (newValue != null)
				newValue.content.onMounted();
			
			if (newValue != null && bounds != null) {
				DIRectangle contentBounds = new DIRectangle(
					bounds.x, bounds.y + totalTabHeight,
					bounds.width, bounds.height - totalTabHeight);
				newValue.content.setBounds(contentBounds);
			}
			if (newValue == null && context != null && bounds != null)
				context.repaint(bounds);
		});
	}
	
	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		path = parent.getChild("tabbedView", styleClass);
		style = new TabbedViewStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.tabFont);
		totalTabHeight = style.tabBorder.getPaddingVertical() + fontMetrics.getAscent() + fontMetrics.getDescent();
		
		for (TabbedViewComponent tab : tabs)
			prepare(tab);
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
		return -1;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		
		if (currentTab.getValue() == null)
			return;
		
		DIRectangle contentBounds = new DIRectangle(
				bounds.x, bounds.y + totalTabHeight,
				bounds.width, bounds.height - totalTabHeight);
		currentTab.getValue().content.setBounds(contentBounds);
		layoutTabs();
	}

	@Override
	public void paint(DCanvas canvas) {
		for (DComponent component : tabComponents)
			component.paint(canvas);
		
		if (currentTab.getValue() != null)
			currentTab.getValue().content.paint(canvas);
	}

	@Override
	public void close() {
		for (Map.Entry<TabbedViewTab, ListenerHandle<LiveObject.Listener<DSizing>>> entry : tabSizeListeners.entrySet()) {
			entry.getValue().close();
		}
	}
	
	private void repaintTabs() {
		context.repaint(bounds.x, bounds.y, bounds.width, totalTabHeight);
	}
	
	private void prepare(TabbedViewComponent tab) {
		tab.content.setContext(path, context);
	}
	
	private void layoutTabs() {
		if (bounds == null)
			return;
		
		int x = bounds.x + style.tabBarSpacingLeft;
		for (DComponent tab : tabComponents) {
			DSizing preferences = tab.getSizing().getValue();
			tab.setBounds(new DIRectangle(
					x, bounds.y + totalTabHeight - preferences.preferredHeight, preferences.preferredWidth, preferences.preferredHeight));
			
			x += preferences.preferredWidth + style.tabSpacing;
		}
		
		repaintTabs();
	}

	@Override
	protected void forEachChild(Consumer<DComponent> children) {
		if (currentTab.getValue() != null)
			children.accept(currentTab.getValue().content);
		
		for (TabbedViewTab component : tabComponents)
			children.accept(component.closeButton);
		for (TabbedViewTab component : tabComponents)
			children.accept(component);
	}

	@Override
	protected DComponent findChild(Predicate<DComponent> predicate) {
		if (currentTab.getValue() != null && predicate.test(currentTab.getValue().content))
			return currentTab.getValue().content;
		
		for (TabbedViewTab component : tabComponents)
			if (predicate.test(component.closeButton))
				return component.closeButton;
		for (TabbedViewTab component : tabComponents)
			if (predicate.test(component))
				return component;
		
		return null;
	}
	
	private class TabListListener implements LiveList.Listener<TabbedViewComponent> {
		@Override
		public void onInserted(int index, TabbedViewComponent value) {
			prepare(value);
			layoutTabs();
			
			if (currentTab.getValue() == null)
				currentTab.setValue(value);
		}

		@Override
		public void onChanged(int index, TabbedViewComponent oldValue, TabbedViewComponent newValue) {
			repaintTabs();
		}

		@Override
		public void onRemoved(int index, TabbedViewComponent oldValue) {
			if (oldValue == currentTab.getValue())
				currentTab.setValue(tabs.size() == 0 ? null : tabs.get(Math.max(index - 1, 0)));
			
			layoutTabs();
		}
	}
}
