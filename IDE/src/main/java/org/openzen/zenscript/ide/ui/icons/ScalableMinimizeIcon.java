/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;

import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.draw.DDrawTarget;

/**
 *
 * @author Hoofdgebruiker
 */
public class ScalableMinimizeIcon implements DColorableIcon {
	private final float scale;
	private final DPath path;
	
	public ScalableMinimizeIcon(float scale) {
		this.scale = scale;
		path = tracer -> {
			tracer.moveTo(scale * 8, scale * 14);
			tracer.lineTo(scale * 16, scale * 14);
		};
	}
	
	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform, int color) {
		target.strokePath(z, path, transform, color, 1);
	}

	@Override
	public float getNominalWidth() {
		return 24 * scale;
	}

	@Override
	public float getNominalHeight() {
		return 24 * scale;
	}
}
