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
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.live.LiveString;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class StatusBarView implements DComponent {
	private final SimpleLiveObject<DSizing> dimensionPreferences = new SimpleLiveObject<>(new DSizing(0, 0));
	
	private final DStyleClass styleClass;
	private final LiveString content;
	private DIRectangle bounds;
	private DDrawSurface surface;
	private int z;
	private StatusBarStyle style;
	private DFontMetrics fontMetrics;

	private DDrawnShape shape;
	private DDrawnText text;
	
	public StatusBarView(DStyleClass styleClass, LiveString content) {
		this.styleClass = styleClass;
		this.content = content;
	}
	
	@Override
	public void mount(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		
		DStylePath path = parent.getChild("StatusBar", styleClass);
		style = new StatusBarStyle(surface.getStylesheet(path));
		fontMetrics = surface.getFontMetrics(style.font);
		
		dimensionPreferences.setValue(new DSizing(0, style.paddingTop + fontMetrics.getAscent() + fontMetrics.getDescent() + style.paddingBottom));
		text = surface.drawText(z + 1, style.font, style.textColor, 0, 0, content.getValue());
	}
	
	@Override
	public void unmount() {
		if (shape != null)
			shape.close();
		if (text != null)
			text.close();
	}
	
	@Override
	public LiveObject<DSizing> getSizing() {
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
		
		if (shape != null)
			shape.close();
		shape = surface.shadowPath(z, DPath.rectangle(bounds.x, bounds.y, bounds.width, bounds.height), DTransform2D.IDENTITY, style.backgroundColor, style.shadow);
		text.setPosition(bounds.x + style.paddingLeft, bounds.y + style.paddingTop + fontMetrics.getAscent());
	}

	@Override
	public void close() {
		unmount();
	}
}
