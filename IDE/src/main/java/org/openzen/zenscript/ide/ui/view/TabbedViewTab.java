/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewTab implements DComponent {
	private final TabbedViewComponent tab;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final MutableLiveObject<TabbedViewComponent> currentTab;
	
	private final TabbedView parent;
	public final TabbedViewTabClose closeButton;
	
	private DDrawSurface surface;
	private TabbedViewTabStyle style;
	private DFontMetrics fontMetrics;
	private int textWidth;
	private DIRectangle bounds;
	
	private final ListenerHandle<LiveString.Listener> titleListener;
	private final ListenerHandle<LiveBool.Listener> updatedListener;
	
	private boolean hover;
	private boolean press;
	
	public TabbedViewTab(TabbedView parent, MutableLiveObject<TabbedViewComponent> currentTab, TabbedViewComponent tab) {
		this.parent = parent;
		this.currentTab = currentTab;
		this.tab = tab;
		this.closeButton = new TabbedViewTabClose(this);
		
		titleListener = tab.title.addListener((oldValue, newValue) -> calculateSizing());
		updatedListener = tab.updated.addListener((oldValue, newValue) -> calculateSizing());
	}
	
	public void closeTab() {
		parent.tabs.remove(tab);
	}

	@Override
	public void setSurface(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		DStylePath path = parent.getChild("tab", DStyleClass.EMPTY);
		style = new TabbedViewTabStyle(surface.getStylesheet(path));
		fontMetrics = surface.getFontMetrics(style.tabFont);
		closeButton.setSurface(path, z + 1, surface);
		
		calculateSizing();
	}
	
	private void calculateSizing() {
		textWidth = fontMetrics.getWidth(tab.title.getValue());
		
		int width = style.paddingLeft + textWidth + style.paddingRight
						+ style.closeIconPadding
						+ closeButton.getSizing().getValue().preferredWidth;
		
		if (tab.updated.getValue()) {
			width += style.updatedDiameter + style.updatedPadding;
		}
		
		sizing.setValue(new DSizing(
				width,
				style.paddingTop + fontMetrics.getAscent() + fontMetrics.getDescent()
						+ style.paddingBottom));
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
		closeButton.setBounds(new DIRectangle(
				bounds.x + bounds.width - close.preferredWidth - style.paddingRight,
				bounds.y + (bounds.height - close.preferredHeight) / 2,
				close.preferredWidth,
				close.preferredHeight));
	}

	@Override
	public void paint(DCanvas canvas) {
		int width = style.paddingLeft + textWidth + style.paddingRight
				+ closeButton.getSizing().getValue().preferredWidth + style.closeIconPadding;
		
		if (tab.updated.getValue())
			width += style.updatedDiameter + style.updatedPadding;

		int color = style.tabColorNormal;
		if (currentTab.getValue() == tab)
			color = style.tabColorActive;
		else if (press)
			color = style.tabColorPress;
		else if (hover)
			color = style.tabColorHover;

		canvas.fillRectangle(bounds.x, bounds.y, width, bounds.height, color);
		canvas.strokePath(DPath.rectangle(bounds.x, bounds.y, width, bounds.height), DTransform2D.IDENTITY, style.borderColor, style.borderWidth);
		
		canvas.drawText(style.tabFont, style.tabFontColor, bounds.x + style.paddingLeft, bounds.y + style.paddingTop + fontMetrics.getAscent(), tab.title.getValue());
		
		if (tab.updated.getValue()) {
			canvas.fillPath(
					DPath.circle(
						bounds.x + bounds.width - style.paddingRight - closeButton.getBounds().width - style.closeIconPadding - style.updatedDiameter / 2,
						bounds.y + style.paddingTop + ((bounds.height - style.paddingTop - style.paddingBottom) / 2), style.updatedDiameter / 2),
					DTransform2D.IDENTITY,
					style.updatedColor);
		}
		
		closeButton.paint(canvas);
	}

	@Override
	public void close() {
		titleListener.close();
		updatedListener.close();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		repaint();
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		press = false;
		repaint();
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		press = true;
		repaint();
	}
	
	@Override
	public void onMouseRelease(DMouseEvent e) {
		press = false;
		repaint();
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		currentTab.setValue(tab);
	}
	
	private void repaint() {
		if (surface != null && bounds != null)
			surface.repaint(bounds);
	}
}
