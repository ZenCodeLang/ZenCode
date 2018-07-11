/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewTab implements DComponent {
	private final TabbedViewComponent tab;
	private final LiveObject<DDimensionPreferences> preferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private final LiveObject<TabbedViewComponent> currentTab;
	
	private final TabbedView parent;
	public final TabbedViewTabClose closeButton;
	
	private DUIContext context;
	private TabbedViewTabStyle style;
	private DFontMetrics fontMetrics;
	private int textWidth;
	private DIRectangle bounds;
	
	private boolean hover;
	private boolean press;
	
	public TabbedViewTab(TabbedView parent, LiveObject<TabbedViewComponent> currentTab, TabbedViewComponent tab) {
		this.parent = parent;
		this.currentTab = currentTab;
		this.tab = tab;
		this.closeButton = new TabbedViewTabClose(this);
	}
	
	public void closeTab() {
		parent.tabs.remove(tab);
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		DStylePath path = parent.getChild("tab", DStyleClass.EMPTY);
		style = new TabbedViewTabStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.tabFont);
		closeButton.setContext(path, context);
		
		textWidth = fontMetrics.getWidth(tab.title);
		preferences.setValue(new DDimensionPreferences(
				style.paddingLeft + textWidth + style.paddingRight
						+ style.closeIconPadding
						+ closeButton.getDimensionPreferences().getValue().preferredWidth,
				style.paddingTop + fontMetrics.getAscent() + fontMetrics.getDescent()
						+ style.paddingBottom));
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return preferences;
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
		
		DDimensionPreferences close = closeButton.getDimensionPreferences().getValue();
		closeButton.setBounds(new DIRectangle(
				bounds.x + bounds.width - close.preferredWidth - style.paddingRight,
				bounds.y + (bounds.height - close.preferredHeight) / 2,
				close.preferredWidth,
				close.preferredHeight));
	}

	@Override
	public void paint(DCanvas canvas) {
		int width = style.paddingLeft + textWidth + style.paddingRight
				+ closeButton.getDimensionPreferences().getValue().preferredWidth + style.closeIconPadding;

		int color = style.tabColorNormal;
		if (currentTab.getValue() == tab)
			color = style.tabColorActive;
		else if (press)
			color = style.tabColorPress;
		else if (hover)
			color = style.tabColorHover;

		canvas.fillRectangle(bounds.x, bounds.y, width, bounds.height, color);
		canvas.strokePath(DPath.rectangle(bounds.x, bounds.y, width, bounds.height), DTransform2D.IDENTITY, style.borderColor, style.borderWidth);

		canvas.drawText(style.tabFont, style.tabFontColor, bounds.x + style.paddingLeft, bounds.y + style.paddingTop + fontMetrics.getAscent(), tab.title);
		
		closeButton.paint(canvas);
	}

	@Override
	public void close() {
		
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
		if (context != null && bounds != null)
			context.repaint(bounds);
	}
}
