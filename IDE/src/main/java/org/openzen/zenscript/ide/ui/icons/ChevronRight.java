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

public class ChevronRight implements DColorableIcon {
	public static final ChevronRight INSTANCE = new ChevronRight();
	
	private ChevronRight() {}
	
	private static final DPath PATH_0 = tracer -> {
		tracer.moveTo(10f, 6f);
		tracer.lineTo(8.59f, 7.41f);
		tracer.lineTo(13.17f, 12f);
		tracer.lineTo(8.59f, 16.59f);
		tracer.lineTo(10f, 18f);
		tracer.lineTo(16.0f, 12.0f);
		tracer.close();
	};
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		canvas.fillPath(PATH_0, transform, color);
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
