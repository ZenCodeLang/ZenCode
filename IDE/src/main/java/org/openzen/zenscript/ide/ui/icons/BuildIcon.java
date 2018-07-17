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
import org.openzen.drawablegui.style.DShadow;

public class BuildIcon implements DColorableIcon {
	public static final BuildIcon INSTANCE = new BuildIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	public static final ColoredIcon BLUE = new ColoredIcon(INSTANCE, 0xFF0652DD);
	
	private BuildIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(22.7f, 19f);
		tracer.lineTo(13.6f, 9.9f);
		tracer.bezierCubic(14.5f, 7.5999994f, 14.0f, 4.8999996f, 12.1f, 2.9999995f);
		tracer.bezierCubic(10.1f, 0.9999995f, 7.1000004f, 0.5999994f, 4.7000003f, 1.6999996f);
		tracer.lineTo(9f, 6f);
		tracer.lineTo(6f, 9f);
		tracer.lineTo(1.6f, 4.7f);
		tracer.bezierCubic(0.4f, 7.1f, 0.9f, 10.1f, 2.9f, 12.1f);
		tracer.bezierCubic(4.8f, 14.0f, 7.5f, 14.5f, 9.8f, 13.6f);
		tracer.lineTo(18.900002f, 22.7f);
		tracer.bezierCubic(19.300001f, 23.1f, 19.900002f, 23.1f, 20.300001f, 22.7f);
		tracer.lineTo(22.6f, 20.400002f);
		tracer.bezierCubic(23.1f, 20.000002f, 23.1f, 19.300001f, 22.7f, 19.000002f);
		tracer.close();
	};
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		canvas.shadowPath(PATH, transform, color, new DShadow(0xFFCCCCCC, 0, 1, 4));
	}
	
	@Override
	public void draw(DDrawTarget target, int z, DTransform2D transform, int color) {
		target.shadowPath(z, PATH, transform, color, new DShadow(0xFFCCCCCC, 0, 1, 4));
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
