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
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class StatusBarView implements DComponent {
	private final SimpleLiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(new DDimensionPreferences(0, 0));
	
	private final DStyleClass styleClass;
	private final LiveString content;
	private DIRectangle bounds;
	private DUIContext context;
	private StatusBarStyle style;
	private DFontMetrics fontMetrics;

	public StatusBarView(DStyleClass styleClass, LiveString content) {
		this.styleClass = styleClass;
		this.content = content;
	}
	
	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		
		DStylePath path = parent.getChild("StatusBar", styleClass);
		style = new StatusBarStyle(context.getStylesheets().get(context, path));
		fontMetrics = context.getFontMetrics(style.font);
		
		dimensionPreferences.setValue(new DDimensionPreferences(0, style.paddingTop + fontMetrics.getAscent() + fontMetrics.getDescent() + style.paddingBottom));
	}
	
	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		return style.paddingTop + fontMetrics.getAscent();
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		canvas.drawText(style.font, style.textColor, style.paddingLeft, style.paddingTop + fontMetrics.getAscent(), content.getValue());
	}

	@Override
	public void close() {
		// nothing to clean up
	}
}
