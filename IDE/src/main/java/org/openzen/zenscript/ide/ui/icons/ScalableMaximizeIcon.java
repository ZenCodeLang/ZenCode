/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;

import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DColorableIcon;
import org.openzen.drawablegui.draw.DDrawTarget;

public class ScalableMaximizeIcon implements DColorableIcon {
	private final DPath path;
	private final float size;
	
	public ScalableMaximizeIcon(float scale) {
		size = 24 * scale;
		path = tracer -> {
			tracer.moveTo(scale * 8, scale * 8);
			tracer.lineTo(scale * 16, scale * 8);
			tracer.lineTo(scale * 16, scale * 16);
			tracer.lineTo(scale * 8, scale * 16);
			tracer.close();
		};
	}
	
	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform, int color) {
		target.strokePath(z, path, transform, color, 1);
	}

	@Override
	public float getNominalWidth() {
		return size;
	}

	@Override
	public float getNominalHeight() {
		return size;
	}
}
