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

public class ColorableAddBoxIcon implements DColorableIcon {
	public static final ColorableAddBoxIcon INSTANCE = new ColorableAddBoxIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	
	private ColorableAddBoxIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(19f, 3f);
		tracer.lineTo(5.0f, 3.0f);
		tracer.bezierCubic(3.8899999f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f);
		tracer.lineTo(3.0f, 19.0f);
		tracer.bezierCubic(3.0f, 20.1f, 3.8899999f, 21.0f, 5.0f, 21.0f);
		tracer.lineTo(19.0f, 21.0f);
		tracer.bezierCubic(20.1f, 21.0f, 21.0f, 20.1f, 21.0f, 19.0f);
		tracer.lineTo(21.0f, 5.0f);
		tracer.bezierCubic(21.0f, 3.9f, 20.1f, 3.0f, 19.0f, 3.0f);
		tracer.close();
		tracer.moveTo(17.0f, 13.0f);
		tracer.lineTo(13.0f, 13.0f);
		tracer.lineTo(13.0f, 17.0f);
		tracer.lineTo(11.0f, 17.0f);
		tracer.lineTo(11.0f, 13.0f);
		tracer.lineTo(7.0f, 13.0f);
		tracer.lineTo(7.0f, 11.0f);
		tracer.lineTo(11.0f, 11.0f);
		tracer.lineTo(11.0f, 7.0f);
		tracer.lineTo(13.0f, 7.0f);
		tracer.lineTo(13.0f, 11.0f);
		tracer.lineTo(17.0f, 11.0f);
		tracer.lineTo(17.0f, 13.0f);
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
