/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;

import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DColorableIcon;

public class ScalableCloseIcon implements DColorableIcon {
	private final DPath path;
	private final float size;
	
	public ScalableCloseIcon(float scale) {
		size = 24 * scale;
		path = tracer -> {
			tracer.moveTo(scale * 8, scale * 8);
			tracer.lineTo(scale * 16, scale * 16);
			tracer.moveTo(scale * 8, scale * 16);
			tracer.lineTo(scale * 16, scale * 8);
		};
	}
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		canvas.strokePath(path, transform, color, 1);
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
