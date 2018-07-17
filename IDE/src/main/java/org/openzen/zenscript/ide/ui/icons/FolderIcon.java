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

public class FolderIcon implements DColorableIcon {
	public static final FolderIcon INSTANCE = new FolderIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	
	private FolderIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(10f, 4f);
		tracer.lineTo(4.0f, 4.0f);
		tracer.bezierCubic(2.9f, 4.0f, 2.01f, 4.9f, 2.01f, 6.0f);
		tracer.lineTo(2f, 18f);
		tracer.bezierCubic(2.0f, 19.1f, 2.9f, 20.0f, 4.0f, 20.0f);
		tracer.lineTo(20.0f, 20.0f);
		tracer.bezierCubic(21.1f, 20.0f, 22.0f, 19.1f, 22.0f, 18.0f);
		tracer.lineTo(22.0f, 8.0f);
		tracer.bezierCubic(22.0f, 6.9f, 21.1f, 6.0f, 20.0f, 6.0f);
		tracer.lineTo(12.0f, 6.0f);
		tracer.lineTo(10.0f, 4.0f);
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
