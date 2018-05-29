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

public class ColorableModuleIcon implements DColorableIcon {
	public static final ColorableModuleIcon INSTANCE = new ColorableModuleIcon();
	
	private ColorableModuleIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(3f, 19f);
		tracer.lineTo(9.0f, 19.0f);
		tracer.lineTo(9.0f, 12.0f);
		tracer.lineTo(3.0f, 12.0f);
		tracer.lineTo(3.0f, 19.0f);
		tracer.close();
		tracer.moveTo(10.0f, 19.0f);
		tracer.lineTo(22.0f, 19.0f);
		tracer.lineTo(22.0f, 12.0f);
		tracer.lineTo(10.0f, 12.0f);
		tracer.lineTo(10.0f, 19.0f);
		tracer.close();
		tracer.moveTo(3f, 5f);
		tracer.lineTo(3.0f, 11.0f);
		tracer.lineTo(22.0f, 11.0f);
		tracer.lineTo(22.0f, 5.0f);
		tracer.lineTo(3.0f, 5.0f);
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
