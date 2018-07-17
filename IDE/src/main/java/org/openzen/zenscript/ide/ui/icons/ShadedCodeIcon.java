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
import org.openzen.drawablegui.draw.DDrawTarget;

public class ShadedCodeIcon implements DColorableIcon {
	public static final ShadedCodeIcon INSTANCE = new ShadedCodeIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	public static final ColoredIcon BLUE = new ColoredIcon(INSTANCE, 0xFF1B1464);
	
	private ShadedCodeIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(9.4f, 16.6f);
		tracer.lineTo(4.8f, 12f);
		tracer.lineTo(9.4f, 7.4f);
		tracer.lineTo(8f, 6f);
		tracer.lineTo(2.0f, 12.0f);
		tracer.lineTo(8.0f, 18.0f);
		tracer.lineTo(9.4f, 16.6f);
		tracer.close();
		tracer.moveTo(14.599999f, 16.6f);
		tracer.lineTo(19.199999f, 12.0f);
		tracer.lineTo(14.599998f, 7.4f);
		tracer.lineTo(16f, 6f);
		tracer.lineTo(22.0f, 12.0f);
		tracer.lineTo(16.0f, 18.0f);
		tracer.lineTo(14.6f, 16.6f);
		tracer.close();
	};
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		canvas.fillPath(PATH, transform, color);
	}
	
	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform, int color) {
		target.fillPath(z, PATH, transform, color);
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
