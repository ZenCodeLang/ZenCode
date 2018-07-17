/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DDrawable;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawTarget;

/**
 *
 * @author Hoofdgebruiker
 */
public class ColoredIcon implements DDrawable {
	private final DColorableIcon icon;
	private final int color;
	
	public ColoredIcon(DColorableIcon icon, int color) {
		this.icon = icon;
		this.color = color;
	}

	@Override
	public void draw(DCanvas canvas, DTransform2D transform) {
		icon.draw(canvas, transform, color);
	}

	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform) {
		icon.draw(target, z, transform, color);
	}

	@Override
	public float getNominalWidth() {
		return icon.getNominalWidth();
	}

	@Override
	public float getNominalHeight() {
		return icon.getNominalHeight();
	}
}
