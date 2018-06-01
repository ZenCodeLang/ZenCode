/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.listeners.DIRectangle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;

/**
 *
 * @author Hoofdgebruiker
 */
public class AspectBarSelectorButton implements DComponent {
	
	public final LiveBool active = new LiveBool();
	
	private final DStyleClass styleClass;
	private final DDrawable icon;
	private final LiveObject<DDimensionPreferences> preferences = new SimpleLiveObject<>(DDimensionPreferences.EMPTY);
	private DUIContext context;
	private AspectBarSelectorButtonStyle style;
	private DIRectangle bounds;
	private DPath shape;
	
	public AspectBarSelectorButton(DStyleClass styleClass, DDrawable icon) {
		this.styleClass = styleClass;
		this.icon = icon;
	}

	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		DStylePath path = parent.getChild("selectorbutton", styleClass);
		style = new AspectBarSelectorButtonStyle(context.getStylesheets().get(context, path));
		shape = DPath.roundedRectangle(
				0,
				0,
				style.width,
				style.height,
				style.roundingRadius);
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
		canvas.shadowPath(
					shape,
					DTransform2D.translate(bounds.x, bounds.y),
					style.shadowColor,
					style.shadowOffsetX,
					style.shadowOffsetY,
					style.shadowRadius);
			canvas.fillPath(
					shape,
					DTransform2D.translate(bounds.x, bounds.y),
					style.colorNormal);
			icon.draw(canvas, DTransform2D.scaleAndTranslate(
					bounds.x + (style.width - icon.getNominalWidth() * context.getScale()) / 2,
					bounds.y + (style.height - icon.getNominalHeight() * context.getScale()) / 2,
					context.getScale()));
	}

	@Override
	public void close() {
		// nothing
	}
}
