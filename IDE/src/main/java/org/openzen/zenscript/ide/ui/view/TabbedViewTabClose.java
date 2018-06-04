/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;
import org.openzen.zenscript.ide.ui.icons.ColoredIcon;
import org.openzen.zenscript.ide.ui.icons.ScalableCloseIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewTabClose implements DComponent {
	private final TabbedViewTab tab;
	private final LiveObject<DDimensionPreferences> preferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	
	private DUIContext context;
	private DIRectangle bounds;
	private TabbedViewTabCloseStyle style;
	private DColorableIcon icon;
	
	private boolean hover;
	private boolean press;
	
	public TabbedViewTabClose(TabbedViewTab tab) {
		this.tab = tab;
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("tabClose", DStyleClass.EMPTY);
		style = new TabbedViewTabCloseStyle(context.getStylesheets().get(context, path));
		preferences.setValue(new DDimensionPreferences(style.size, style.size));
		icon = new ScalableCloseIcon(style.size / 24);
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
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		if (hover) {
			canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 0xFFE81123);
		}
		
		icon.draw(canvas, DTransform2D.translate(
				bounds.x + (bounds.width - icon.getNominalWidth()) / 2,
				bounds.y + (bounds.height - icon.getNominalHeight()) / 2),
				hover ? 0xFFFFFFFF : 0xFF000000);
	}

	@Override
	public void close() {
		
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		context.repaint(bounds);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		context.repaint(bounds);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		tab.closeTab();
	}
}
