/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewTab implements DComponent {
	private final TabbedViewComponent tab;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final MutableLiveObject<TabbedViewComponent> currentTab;
	
	private final DStyleClass styleClass;
	private final TabbedView parent;
	public final TabbedViewTabClose closeButton;
	
	private DComponentContext context;
	private TabbedViewTabStyle style;
	private DFontMetrics fontMetrics;
	private int textWidth;
	private DIRectangle bounds;
	
	private final ListenerHandle<LiveString.Listener> titleListener;
	private final ListenerHandle<LiveBool.Listener> updatedListener;
	private final ListenerHandle<LiveObject.Listener<TabbedViewComponent>> currentTabListener;
	
	private boolean hover;
	private boolean press;
	
	private DDrawnShape shape;
	private DDrawnShape updated;
	private DDrawnText text;
	
	public TabbedViewTab(DStyleClass styleClass, TabbedView parent, MutableLiveObject<TabbedViewComponent> currentTab, TabbedViewComponent tab) {
		this.styleClass = styleClass;
		this.parent = parent;
		this.currentTab = currentTab;
		this.tab = tab;
		this.closeButton = new TabbedViewTabClose(this);
		
		titleListener = tab.title.addListener((oldValue, newValue) -> calculateSizing());
		updatedListener = tab.updated.addListener((oldValue, newValue) -> calculateSizing());
		currentTabListener = currentTab.addListener((oldTab, newTab) -> update());
	}
	
	public void closeTab() {
		parent.tabs.remove(tab);
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("tab", styleClass);
		style = context.getStyle(TabbedViewTabStyle::new);
		fontMetrics = context.getFontMetrics(style.tabFont);
		closeButton.mount(context);
		
		text = context.drawText(1, style.tabFont, style.tabFontColor, 0, 0, tab.title.getValue());
		calculateSizing();
	}
	
	@Override
	public void unmount() {
		if (shape != null)
			shape.close();
		if (updated != null)
			updated.close();
		if (text != null)
			text.close();
		
		closeButton.close();
		style.border.close();
	}
	
	private void calculateSizing() {
		textWidth = fontMetrics.getWidth(tab.title.getValue());
		
		int width = style.border.getPaddingHorizontal() + textWidth
						+ style.closeIconPadding
						+ closeButton.getSizing().getValue().preferredWidth;
		if (tab.updated.getValue()) {
			width += style.updatedDiameter + style.updatedPadding;
		}
		
		sizing.setValue(new DSizing(
				width,
				style.border.getPaddingHorizontal() + fontMetrics.getAscent() + fontMetrics.getDescent()));
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
		
		DSizing close = closeButton.getSizing().getValue();
		style.border.update(context, style.margin.apply(bounds));
		
		closeButton.setBounds(new DIRectangle(
				bounds.x + bounds.width - close.preferredWidth - style.border.getPaddingRight(),
				bounds.y + (bounds.height - close.preferredHeight) / 2,
				close.preferredWidth,
				close.preferredHeight));
		
		if (shape != null)
			shape.close();
		shape = context.shadowPath(0, style.shape.instance(style.margin.apply(bounds)), DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
		text.setPosition(
				bounds.x + style.margin.left + style.border.getPaddingLeft(),
				bounds.y + style.margin.top + style.border.getPaddingTop() + fontMetrics.getAscent());
		
		if (tab.updated.getValue()) {
			if (updated != null)
				updated.close();
			updated = context.fillPath(
					1,
					DPath.circle(
						bounds.x + bounds.width - style.margin.right - style.border.getPaddingRight() - closeButton.getBounds().width - style.closeIconPadding - style.updatedDiameter / 2,
						bounds.y + style.margin.top + style.border.getPaddingTop() + ((bounds.height - style.margin.getVertical() - style.border.getPaddingVertical()) / 2), style.updatedDiameter / 2),
					DTransform2D.IDENTITY,
					style.updatedColor);
		} else {
			if (updated != null) {
				updated.close();
				updated = null;
			}
		}
	}

	@Override
	public void close() {
		titleListener.close();
		updatedListener.close();
		
		unmount();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		update();
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		press = false;
		update();
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		press = true;
		update();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		press = false;
		update();
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		currentTab.setValue(tab);
	}
	
	private void update() {
		if (style == null || shape == null)
			return;
		
		int color = style.tabColorNormal;
		if (currentTab.getValue() == tab)
			color = style.tabColorActive;
		else if (press)
			color = style.tabColorPress;
		else if (hover)
			color = style.tabColorHover;
		shape.setColor(color);
	}
}
