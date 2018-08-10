/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DColorableIconInstance;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.zenscript.ide.ui.icons.ScalableCloseIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class TabbedViewTabClose implements DComponent {
	private final TabbedViewTab tab;
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	
	private DComponentContext context;
	private DIRectangle bounds;
	private TabbedViewTabCloseStyle style;
	private DColorableIcon icon;
	
	private boolean hover;
	private boolean press;
	
	private DDrawnRectangle background;
	private DColorableIconInstance drawnIcon;
	
	public TabbedViewTabClose(TabbedViewTab tab) {
		this.tab = tab;
	}

	@Override
	public void mount(DComponentContext parent) {
		context = parent.getChildContext("tabclose", DStyleClass.EMPTY);
		style = context.getStyle(TabbedViewTabCloseStyle::new);
		sizing.setValue(new DSizing(style.size, style.size));
		icon = new ScalableCloseIcon(style.size / 24);
		
		if (background != null)
			background.close();
		background = context.fillRect(1, DIRectangle.EMPTY, hover ? 0xFFE81123 : 0);
		if (drawnIcon != null)
			drawnIcon.close();
		drawnIcon = new DColorableIconInstance(context.surface, context.z + 2, icon, DTransform2D.IDENTITY, 0xFF000000);
	}
	
	@Override
	public void unmount() {
		if (background != null)
			background.close();
		if (drawnIcon != null)
			drawnIcon.close();
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
		
		background.setRectangle(bounds);
		
		if (drawnIcon != null)
			drawnIcon.close();
		drawnIcon = new DColorableIconInstance(context.surface, context.z + 2, icon, DTransform2D.translate(
				bounds.x + (bounds.width - icon.getNominalWidth()) / 2,
				bounds.y + (bounds.height - icon.getNominalHeight()) / 2), 0xFF000000);
	}

	@Override
	public void close() {
		unmount();
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		hover = true;
		background.setColor(0xFFE81123);
		drawnIcon.setColor(0xFFFFFFFF);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		hover = false;
		background.setColor(0);
		drawnIcon.setColor(0xFF000000);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		tab.closeTab();
	}
}
