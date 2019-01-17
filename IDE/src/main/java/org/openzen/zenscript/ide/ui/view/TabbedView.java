/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import listeners.ListenerHandle;
import live.LiveArrayList;
import live.LiveList;
import live.LiveMappedList;
import live.LiveObject;
import live.MutableLiveList;
import live.MutableLiveObject;
import live.SimpleLiveObject;

import org.openzen.drawablegui.BaseComponentGroup;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedView extends BaseComponentGroup {
	private final DStyleClass styleClass;
	public final MutableLiveList<TabbedViewComponent> tabs = new LiveArrayList<>();
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	public final MutableLiveObject<TabbedViewComponent> currentTab = new SimpleLiveObject<>(null);
	
	private final Map<TabbedViewTab, ListenerHandle<BiConsumer<DSizing, DSizing>>> tabSizeListeners = new HashMap<>();
	
	private DComponentContext context;
	private TabbedViewStyle style;
	private DIRectangle bounds;
	private int totalTabHeight;
	private DFontMetrics fontMetrics;

	private final LiveList<TabbedViewTab> tabComponents = new LiveMappedList<>(tabs, tab -> {
		TabbedViewTab result = new TabbedViewTab(DStyleClass.EMPTY, this, currentTab, tab);
		if (context != null)
			result.mount(context);
		
		tabSizeListeners.put(result, result.getSizing().addListener((oldSize, newSize) -> layoutTabs()));
		return result;
	});
	
	public TabbedView(DStyleClass styleClass) {
		this.styleClass = styleClass;
		tabs.addListener(new TabListListener());
		
		currentTab.addListener((oldValue, newValue) -> {
			if (oldValue != null)
				oldValue.content.unmount();
			if (newValue != null)
				newValue.content.mount(context);
			
			if (newValue != null && bounds != null) {
				DIRectangle contentBounds = new DIRectangle(
						bounds.x + style.margin.left,
						bounds.y + style.margin.top + totalTabHeight,
						bounds.width - style.margin.getHorizontal(),
						bounds.height - style.margin.getVertical() - totalTabHeight);
				newValue.content.setBounds(contentBounds);
			}
		});
	}
	
	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("tabbedview", styleClass);
		style = context.getStyle(TabbedViewStyle::new);
		fontMetrics = context.getFontMetrics(style.tabFont);
		totalTabHeight = style.tabBorder.getPaddingVertical() + fontMetrics.getAscent() + fontMetrics.getDescent();
		
		for (TabbedViewComponent tab : tabs)
			prepare(tab);
	}
	
	@Override
	public void unmount() {
		for (TabbedViewComponent tab : tabs)
			tab.content.unmount();
		for (TabbedViewTab tab : tabComponents)
			tab.unmount();
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
				bounds.x + style.margin.left,
				bounds.y + style.margin.top + totalTabHeight,
				bounds.width - style.margin.getHorizontal(),
				bounds.height - style.margin.getVertical() - totalTabHeight);
		currentTab.getValue().content.setBounds(contentBounds);
		layoutTabs();
	}

	@Override
	public void close() {
		for (Map.Entry<TabbedViewTab, ListenerHandle<BiConsumer<DSizing, DSizing>>> entry : tabSizeListeners.entrySet()) {
			entry.getValue().close();
		}
		
		for (TabbedViewComponent tab : tabs)
			tab.content.close();
		for (TabbedViewTab tab : tabComponents)
			tab.close();
	}
	
	private void prepare(TabbedViewComponent tab) {
		tab.content.mount(context);
	}
	
	private void layoutTabs() {
		if (bounds == null)
			return;
		
		int x = bounds.x + style.margin.left + style.tabBarSpacingLeft;
		for (DComponent tab : tabComponents) {
			DSizing preferences = tab.getSizing().getValue();
			tab.setBounds(new DIRectangle(
					x, bounds.y + style.margin.top + totalTabHeight - preferences.preferredHeight, preferences.preferredWidth, preferences.preferredHeight));
			
			x += preferences.preferredWidth + style.tabSpacing;
		}
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
			
		}

		@Override
		public void onRemoved(int index, TabbedViewComponent oldValue) {
			if (oldValue == currentTab.getValue())
				currentTab.setValue(tabs.getLength() == 0 ? null : tabs.getAt(Math.max(index - 1, 0)));
			
			layoutTabs();
		}
	}
}
