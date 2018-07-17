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

public class ProjectIcon implements DColorableIcon {
	public static final ProjectIcon INSTANCE = new ProjectIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	public static final ColoredIcon GREY = new ColoredIcon(INSTANCE, 0xFF888888);
	
	private ProjectIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(3f, 13f);
		tracer.lineTo(11.0f, 13.0f);
		tracer.lineTo(11.0f, 3.0f);
		tracer.lineTo(3.0f, 3.0f);
		tracer.lineTo(3.0f, 13.0f);
		tracer.close();
		tracer.moveTo(3.0f, 21.0f);
		tracer.lineTo(11.0f, 21.0f);
		tracer.lineTo(11.0f, 15.0f);
		tracer.lineTo(3.0f, 15.0f);
		tracer.lineTo(3.0f, 21.0f);
		tracer.close();
		tracer.moveTo(13.0f, 21.0f);
		tracer.lineTo(21.0f, 21.0f);
		tracer.lineTo(21.0f, 11.0f);
		tracer.lineTo(13.0f, 11.0f);
		tracer.lineTo(13.0f, 21.0f);
		tracer.close();
		tracer.moveTo(13.0f, 3.0f);
		tracer.lineTo(13.0f, 9.0f);
		tracer.lineTo(21.0f, 9.0f);
		tracer.lineTo(21.0f, 3.0f);
		tracer.lineTo(13.0f, 3.0f);
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
