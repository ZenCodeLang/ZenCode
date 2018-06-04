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

public class CloseIcon implements DColorableIcon {
	public static final CloseIcon INSTANCE = new CloseIcon();
	
	private CloseIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(19f, 6.41f);
		tracer.lineTo(17.59f, 5f);
		tracer.lineTo(12f, 10.59f);
		tracer.lineTo(6.41f, 5f);
		tracer.lineTo(5f, 6.41f);
		tracer.lineTo(10.59f, 12f);
		tracer.lineTo(5f, 17.59f);
		tracer.lineTo(6.41f, 19f);
		tracer.lineTo(12f, 13.41f);
		tracer.lineTo(17.59f, 19f);
		tracer.lineTo(19f, 17.59f);
		tracer.lineTo(13.41f, 12f);
		tracer.close();
	};
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		canvas.fillPath(PATH, transform, color);
	}

	@Override
	public float getNominalWidth() {
		return 24;
	}

	@Override
	public float getNominalHeight() {
		return 24;
	}
}
